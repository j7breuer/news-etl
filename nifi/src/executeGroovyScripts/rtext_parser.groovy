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
import java.text.SimpleDateFormat
import java.util.TimeZone

flowFile = session.get()
if (!flowFile) return

flowFile = session.write(flowFile, { inputStream, outputStream ->
    // Define json object from content
    def jsonSlurper = new JsonSlurper()
    def row = jsonSlurper.parseText(IOUtils.toString(inputStream, StandardCharsets.UTF_8))
    String textOupt = row.get("data").get("response").get("text")

    // Extract source
    String source_text = null
    def source_pattern = Pattern.compile('Fusion\\.arcSite="([^"]+)"')
    def source_matcher = source_pattern.matcher(textOupt)
    if (source_matcher.find()) {
        source_text = source_matcher.group(1)
    }


    // Extract body
    def content_pattern = Pattern.compile("\"content_elements\"\\s*:\\s*(\\[\\{.*?\\}\\])")
    def content_matcher = content_pattern.matcher(textOupt)
    def author_pattern = Pattern.compile('authors"\\s*:(.*?)\\s*,"word_count"')
    def author_matcher = author_pattern.matcher(textOupt)
    def parsed_text = ''  // Initialize the variable outside the block

    // Replace the matched hyperlink with the captured text, escaping any double quotes
    def startPattern = /<a href=\\\"[^\\]+\\\">([^<]+)/
    def replaceHyperlink = { match ->
        // Escape any double quotes in the captured text
        def linkedText = match[0][1].replace('"', '\\"')
        return linkedText
    }

    // Extract body of article
    try {
        if (content_matcher.find()) {
            def extractedValue = content_matcher.group(1)
            def cleaned_extractedValue = extractedValue.replaceAll(startPattern, replaceHyperlink)
            cleaned_extractedValue = cleaned_extractedValue.replaceAll(/<\/a>/, '')
            def extractedJson = new JsonSlurper().parseText(cleaned_extractedValue)
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
    } catch (Exception e) {
        parsed_text = "Unparsed text: ${e.message}"
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

    // Extract content
    def extracted_section = []
    if (section_matcher.find()){
        extracted_section << section_matcher.group(1).toString().capitalize()
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
    String extracted_description = null
    def description_pattern = Pattern.compile('"description":"(.*?)"')
    def description_matcher = description_pattern.matcher(textOupt)
    if (description_matcher.find()){
        extracted_description = description_matcher.group(1)
    }

    // Extract date created/published
    // In data: published_time -> Schema: published_date
    String extracted_published_date = null
    def published_time_pattern = Pattern.compile("\"published_time\":\"(.*?)\"")
    def published_time_matcher = published_time_pattern.matcher(textOupt)
    if (published_time_matcher.find()){
        extracted_published_date = published_time_matcher.group(1)
    }

    // Convert to Elasticsearch format, starting with original format
    def currentFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    currentFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    def elasticFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
    elasticFormat.setTimeZone(TimeZone.getDefault())
    def parsedDateFinal = ""

    // Parse original string into original format
    try {
        def parsedDate = currentFormat.parse(extracted_published_date)
        parsedDateFinal = elasticFormat.format(parsedDate)
    } catch (Exception e) {
        def now = new Date()
        def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        parsedDateFinal = sdf.format(now)
    }

    // Define output json
    def jsonOupt = [:]
    jsonOupt.put("news_body", parsed_text)
    jsonOupt.put("authors", author_array)
    jsonOupt.put("title", extracted_title)
    jsonOupt.put("section", extracted_section)
    jsonOupt.put("subsection", subsection_name_array)
    jsonOupt.put("abstract", extracted_description)
    jsonOupt.put("published_date", parsedDateFinal)
    jsonOupt.put("created_date", parsedDateFinal)
    jsonOupt.put("first_published_date", parsedDateFinal)
    jsonOupt.put("url", article_url)
    jsonOupt.put("source", source_text.capitalize())
    def ffContent = JsonOutput.toJson(jsonOupt)

    // Output to flowfile
    outputStream.write(ffContent.toString().getBytes(StandardCharsets.UTF_8))
} as StreamCallback)

session.transfer(flowFile, REL_SUCCESS)
