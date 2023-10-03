

# News Data Ingestion ETL Pipeline
Repository of artifacts, scripts, schemas, and more that are utilized by an ETL for gathering the latest news articles across major news providers.  The ETL is designed to run 24/7 and react to newly published articles via RSS feeds by pulling article information (e.g., date-time, title, category, body).

## Development Phases
### First Phase
Primarily data engineering, data mining, and necessary analytics (e.g., language translation) to build the databases used for the ETL.  

The goal is to design the ETL to encompass all major news sources, projecting 1,500-2,000 articles in total gathered daily. 

### Second Phase
The main focus of the second phase is to begin data enrichment of articles with natural language processing.  By adding these additional fields, another dimension of data is added to allow for advanced analytics designed to answer analytical questions of interest. 

### Third Phase and Beyond
The third phase and beyond will look to bring graph analytics into the analytical architecture of the ETL to understand connections across the data sets.  

Beyond the third phase, additional data sets outside of news articles will be added into the ETL for more expansive analytics.

## Schemas

### General Schema:
- URL: The URL of the article after following any redirects
- Title: Title of the article
- Subtitle: Some news sources provide subtitles, though not common
- Section: Main section that the article exists in, can sometimes span across multiple sections
- Subsection: Some news sources provide subsections, though not common
- Source: Original news source (e.g., Reuters, NYTimes, NPR)
- Byline: Text of who contributed to the article
- Authors: All authors that wrote the article
- Abstract: News source provided summarization of the article
- Body: All article body text, parsed to remove URLs and other unwanted values
- Created date: Date-time stamp for when the article was created, not necessarily the same as when it was published
- First published date: Original published date, never changes
- Published date: Latest published date, relevant when an article has been updated

### Elasticsearch:
The data gathered by the ETL is largely text data (e.g., body, title) and best fits the use case for <a href="https://www.elastic.co/">Elasticsearch</a>.  Elasticsearch 7.17 is being used currently with no plans to migrate local cluster to Elasticsearch 8.10.  All Elasticsearch schemas and rollover policies are designed on v7.17.

Currently, all schemas are designed for basic data ingestion and do not account for future analytic processes as outlined further in the README.  

Elasticsearch's rollover capability is used to keep indices from bloating as the data gathering process expands.

As the schema is updated, scripts will be added to the repository to reindex data and are carried out within Kibana's Dev Tools GUI.

### AVRO:
Most webpages, both HTML and JavaScript, follow very similar schemas with slightly different key value pairs for relevant fields pulled out.  Avro is used to enforce the standardization of all variations in keys into a common data model aligning with the Elasticsearch schema. 

## NiFi
### Execute Groovy Scripts:
NiFi is the ETL of choice and used for orchestrating all steps in the data gathering process. When NiFi's out-of-the-box processors can't be used for specific use cases (typically advanced data parsing), custom scripts can be used to manipulate, parse, and analyze data.

Groovy is used by this ETL for these custom use cases. Depending on the news source, advanced regex patterns and data manipulation steps need to be implemented to correctly parse out the fields used by the designed schemas. 

## CI / CD
### Jenkinsfile:

Using Jenkins, Sonarqube, and Nexus as the core CI/CD tools, Jenkins is used to orchestrate the continuous integration and continuous development steps:

1. Pull repository's master branch down onto NiFi servers
2. Transfer any 3rd party assets (e.g., jars) from Nexus onto NiFi servers

Currently, the Jenkinsfile is only outlined with these basic steps but will later be updated to include the code to carry out each step.

## External Services Utilized
### <a href="https://github.com/j7breuer/language-translation-api">Language Translation API</a>:
Depending on the news source, some articles are published for non-English speaking audiences.  English is the standardized language of choice for the ETL and all future analytic steps.

If an article is detected to be in a non-English language (e.g., Spanish, French, German), then the article's data will be sent to the language translation API for translation. 

### <a href="https://github.com/j7breuer/webscraper-api">Webscraper API</a>:
All new source's webpages relevant content for this ETL are either in HTML or JavaScript.  In order to centralize all API calls for gathering news information, all articles pulled from RSS feeds are sent to the webscraper API for the request call to be made.

By sending all webscraping calls through the API, logs are stored in one location and additional steps to ensure anonymity can be added to the Dockerfile.

## Future Integrations
The news ETL is currently in it's first phase of being developed to ingest news articles en masse. Once the first phase is completed, the second phase will begin and focus on utilizing analytics (primarily natural language processing and graph) to enrich the data.

### Text Classifications:
To be designed.

### Sentiment Analysis:
To be designed.

### Bias Analysis:
To be designed.

### Named Entity Recognition:
To be designed.

### Topic Modelling: 
To be designed.
