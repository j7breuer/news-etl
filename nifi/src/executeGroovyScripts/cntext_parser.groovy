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

<<<<<<< HEAD
try {
    flowFile = session.write(flowFile, { inputStream, outputStream ->
        // Define json object from content
        def jsonSlurper = new JsonSlurper()
        def row = jsonSlurper.parseText(IOUtils.toString(inputStream, StandardCharsets.UTF_8))
        String textOupt = row.get("data").get("response").get("text")

        // Get ff attributes
        String article_url = flowFile.getAttribute("url")
        String source = flowFile.getAttribute("source")

        // Extract body
        def content_pattern = Pattern.compile('<script type="application/ld\\+json">(.*?)</script>')
        def content_matcher = content_pattern.matcher(textOupt)
        def parsed_text = ''
        def author_array = []
        def section = []
        def subsection = []
        def published_date = ''
        def description = ''
        def title = ''

        // Extract as much of the schema as possible
        try {
            if (content_matcher.find()) {
                def extractedValue = content_matcher.group(1)
                def extractedJson = new JsonSlurper().parseText(extractedValue)
                parsed_text = extractedJson.articleBody
                section = extractedJson.articleSection.collect {it.capitalize()}
                description = extractedJson.description
                published_date = extractedJson.datePublished
                title = extractedJson.headline
                author_array = extractedJson.author.collect{it['name']}
            }
        } catch (Exception e) {
            parsed_text = "Unparsed text: ${e.message}"
            log.warn(${parsed_text})
        }

        // Convert to Elasticsearch format, starting with original format
        def parsedDateFinal = ""
        if (!published_date) {
            def now = new Date()
            def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
            parsedDateFinal = sdf.format(now)
        } else {
            def currentFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            currentFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            def elasticFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
            elasticFormat.setTimeZone(TimeZone.getDefault())
            // Parse original string into original format
            def parsedDate = currentFormat.parse(published_date)
            parsedDateFinal = elasticFormat.format(parsedDate)
        }

        // Define output json
        def jsonOupt = [:]
        jsonOupt.put("news_body", parsed_text)
        jsonOupt.put("authors", author_array)
        jsonOupt.put("title", title)
        jsonOupt.put("section", section)
        jsonOupt.put("subsection", subsection)
        jsonOupt.put("abstract", description)
        jsonOupt.put("published_date", parsedDateFinal)
        jsonOupt.put("created_date", parsedDateFinal)
        jsonOupt.put("first_published_date", parsedDateFinal)
        jsonOupt.put("url", article_url)
        jsonOupt.put("source", source)
        def ffContent = JsonOutput.toJson(jsonOupt)

        // Output to flowfile
        outputStream.write(ffContent.toString().getBytes(StandardCharsets.UTF_8))

    } as StreamCallback)

    // Move flowfile to success queue
    session.transfer(flowFile, REL_SUCCESS)

} catch (Exception e) {

    // Drop flowfile to failure queue
    session.transfer(flowFile, REL_FAILURE)

}
=======
flowFile = session.write(flowFile, { inputStream, outputStream ->
    // Define json object from content
    def jsonSlurper = new JsonSlurper()
    def row = jsonSlurper.parseText(IOUtils.toString(inputStream, StandardCharsets.UTF_8))
    String textOupt = row.get("data").get("response").get("text")

    // Get ff attributes
    String article_url = flowFile.getAttribute("url")
    String source = flowFile.getAttribute("source")

    // Extract body
    def content_pattern = Pattern.compile('<script type="application/ld\\+json">(.*?)</script>')
    def content_matcher = content_pattern.matcher(textOupt)
    def parsed_text = ''
    def author_array = []
    def section = []
    def subsection = []
    def published_date = ''
    def description = ''
    def title = ''

    // Extract as much of the schema as possible
    try {
        if (content_matcher.find()) {
            def extractedValue = content_matcher.group(1)
            def extractedJson = new JsonSlurper().parseText(extractedValue)
            parsed_text = extractedJson.articleBody
            section = extractedJson.articleSection.collect {it.capitalize()}
            description = extractedJson.description
            published_date = extractedJson.datePublished
            title = extractedJson.headline
            author_array = extractedJson.author.collect{it['name']}
        }
    } catch (Exception e) {
        parsed_text = "Unparsed text: ${e.message}"
        log.warn(${parsed_text})
    }

    // Convert to Elasticsearch format, starting with original format
    def parsedDateFinal = ""
    if (!published_date) {
        def now = new Date()
        def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        parsedDateFinal = sdf.format(now)
    } else {
        def currentFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        currentFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        def elasticFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        elasticFormat.setTimeZone(TimeZone.getDefault())
        // Parse original string into original format
        def parsedDate = currentFormat.parse(published_date)
        parsedDateFinal = elasticFormat.format(parsedDate)
    }


    // Define output json
    def jsonOupt = [:]
    jsonOupt.put("news_body", parsed_text)
    jsonOupt.put("authors", author_array)
    jsonOupt.put("title", title)
    jsonOupt.put("section", section)
    jsonOupt.put("subsection", subsection)
    jsonOupt.put("abstract", description)
    jsonOupt.put("published_date", parsedDateFinal)
    jsonOupt.put("created_date", parsedDateFinal)
    jsonOupt.put("first_published_date", parsedDateFinal)
    jsonOupt.put("url", article_url)
    jsonOupt.put("source", source)
    def ffContent = JsonOutput.toJson(jsonOupt)

    // Output to flowfile
    outputStream.write(ffContent.toString().getBytes(StandardCharsets.UTF_8))

} as StreamCallback)

session.transfer(flowFile, REL_SUCCESS)
>>>>>>> cf4b61b0d2860ba9bbc4d2711195a727bf01cf0b
