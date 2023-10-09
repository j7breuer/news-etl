import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import groovy.json.JsonOutput
import org.apache.commons.io.IOUtils
import org.apache.tika.language.detect.LanguageDetector
import org.apache.tika.language.detect.LanguageResult
import org.apache.tika.config.TikaConfig

flowFile = session.get()
if (!flowFile) return

try {
    flowFile = session.write(flowFile, { inputStream, outputStream ->

        // Read in body
        def jsonSlurper = new JsonSlurper()
        def row = jsonSlurper.parseText(IOUtils.toString(inputStream, StandardCharsets.UTF_8))
        String originalBody = row.get("body")
        String detectedLanguage = ""

        // Create tike config and get lang detector
        try {
            TikaConfig tikaConfig = new TikaConfig()
            LanguageDetector detector = LanguageDetector.getDefaultLanguageDetector();
            detector.loadModels();
            LanguageResult result = detector.detect(originalBody)
            detectedLanguage = result.getLanguage()
        } catch (Exception e) {
            log.warn(e.message)
            detectedLanguage = "undetected"
        }

        // Add to ff content
        row.put("detected_language", detectedLanguage)

        // Write back to nifi
        def jsonOutput = JsonOutput.toJson(row)
        outputStream.write(jsonOutput.getBytes(StandardCharsets.UTF_8))

    } as StreamCallback)

    // Move to success
    session.transfer(flowFile, REL_SUCCESS)

} catch (Exception e) {
    log.warn(e.message)
    // Drop flowfile into failure queue
    session.transfer(flowFile, REL_FAILURE)

}