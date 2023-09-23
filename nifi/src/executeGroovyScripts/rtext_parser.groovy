import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import java.util.Map
import java.util.List
import java.io.File
import java.io.InputStream
import java.io.FileInputStream
import java.lang.Object
import groovy.json.JsonOutput
import org.apache.commons.io.IOUtils
import java.util.regex.Pattern
import java.util.regex.Matcher

flowFile = session.get()
if (!flowFile) return

flowFile = session.write(flowFile, { inputStream, outputStream ->
    // Define json object from content
    def jsonSlurper = new JsonSlurper()
    def row = jsonSlurper.parseText(IOUtils.toString(inputStream, StandardCharsets.UTF_8))
    String textOupt = row.get("data").get("response").get("text")

    // Define regex pattern and apply
    def content_pattern = Pattern.compile("\"content_elements\"\\s*:\\s*(\\[\\{.*?\\}\\])")
    def content_matcher = content_pattern.matcher(textOupt)
    def author_pattern = Pattern.compile('authors"\\s*:(.*?)\\s*,"word_count"')
    def author_matcher = author_pattern.matcher(textOupt)
    def parsed_text = ''  // Initialize the variable outside the block

    // Extract body of article
    if (content_matcher.find()) {
        def extractedValue = content_matcher.group(1)
        def extractedJson = new JsonSlurper().parseText(extractedValue)
        parsed_text = extractedJson.collect { entry ->
            switch (entry.type) {
                case 'paragraph':
                    return entry.content + '\n'
                case 'header':
                    return entry.content + '\n\n'
                default:
                    return ''
            }
        }.join()
    }

    // Extract authors
    def author_array = []
    if (author_matcher.find()) {
        def extractedValue = author_matcher.group(1)
        def extractedJson = new JsonSlurper().parseText(extractedValue)
        extractedJson.each { extractedJsonObject ->
            author_array << extractedJsonObject.name.replace("\n", "")
        }
    }

    // Extract title
    //def title_pattern = Pattern.compile('"title"\\s*:\\s*"(.*?)(?=\\s*\\"content_elements")')
    def title_pattern = Pattern.compile('"title"\\s*:\\s*"([^"]+)"\\s*,\\s*"content_elements"', Pattern.DOTALL)
    def title_matcher = title_pattern.matcher(textOupt)
    String extracted_title = null
    if (title_matcher.find()){
        extracted_title = title_matcher.group(1).toString()
    }

    // Extract section
    // Incoming data: other_sections: [{"name" key}] (there can be multiple potentially bc it is an array) -> Schema: subsection
    String article_url = flowFile.getAttribute("url")
    def section_pattern = Pattern.compile("(?<=\\.com/)([^/]+)")
    def subsection_pattern = Pattern.compile('\"other_sections\":(\\[.*?\\])')
    def section_matcher = section_pattern.matcher(article_url)
    def subsection_matcher = subsection_pattern.matcher(textOupt)

    // Extract section
    String extracted_section = null
    if (section_matcher.find()){
        extracted_section = section_matcher.group(1).toString()
    }
    
    // Extract subsection
    String extracted_subsection = null
    def subsection_name_array = new ArrayList()
    if (subsection_matcher.find()){
        extracted_subsection = subsection_matcher.group(1).toString()
        def subsectionJson = new JsonSlurper().parseText(extracted_subsection)
        subsectionJson.each { section ->
            subsection_name_array.add(section.name)
        }
    }

    // Extract abstract/byline/description: First it is description, in schema it is "abstract".  Description -> abstract


    // Extract date created/published
    // In data: published_time -> Schema: published_date


    // Define output json
    def jsonOupt = [:]
    jsonOupt.put("news_body", parsed_text)
    jsonOupt.put("authors", author_array)
    jsonOupt.put("title", extracted_title)
    jsonOupt.put("section", extracted_section.capitalize())
    jsonOupt.put("subsection", subsection_name_array)
    def ffContent = JsonOutput.toJson(jsonOupt)

    // Output to flowfile
    outputStream.write(ffContent.toString().getBytes(StandardCharsets.UTF_8))
} as StreamCallback)

session.transfer(flowFile, REL_SUCCESS)
