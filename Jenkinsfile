pipeline {
    agent any

    environment {
        jarsArray = [
            "tika-langdetect-1.28.5.jar",
            "tika-langdetect-optimaize-2.9.0.jar",
            "tika-core-2.9.0.jar",
            "tika-app-2.4.1.jar",
            "tika-eval-app-2.4.1.jar",
            "tika-parser-scientific-package-2.4.1.jar"
        ]
    }

    stages {
        stage('Environment Setup') {
            steps {
                echo '\n=======================\n[START] Initializing...\n=======================\n'
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL} \n"
                echo "Beginning ETL build"
                echo '\n=====================\n[END] Initializing...\n=====================\n'
            }
        }
        stage('Repository clone and file transfer from Nexus') {
            steps {
                echo '\n============================\n[START] Repository file transfer started...\n============================\n'
                echo "Transferring  to node 1: ${env.NIFI_NODE_1}"
                sshagent(credentials: ['nifi-node-1']) {
                    withCredentials([usernamePassword(credentialsId: 'nexus-login', passwordVariable: 'NEXUS_PASSWORD', usernameVariable: 'NEXUS_USERNAME')]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no user@${env.NIFI_NODE_1} "
                            rm -rf news-etl &&
                            git clone https://github.com/j7breuer/news-etl.git &&
                            mkdir ./news-etl/jars &&
                            cd ./news-etl/jars &&
                            ${jarsArray.collect { "curl -u \$NEXUS_USERNAME:\$NEXUS_PASSWORD -O ${env.NEXUS}:8081/repository/3rd-party/jars/${it}" }.join(" && ")}
                        "
                    """
                    }
                }
                echo 'Transferring  to node 2: ${env.NIFI_NODE_2}'
                sshagent(credentials: ['nifi-node-2']) {
                    withCredentials([usernamePassword(credentialsId: 'nexus-login', passwordVariable: 'NEXUS_PASSWORD', usernameVariable: 'NEXUS_USERNAME')]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no user@${env.NIFI_NODE_2} "
                                rm -rf news-etl &&
                                git clone https://github.com/j7breuer/news-etl.git &&
                                mkdir ./news-etl/jars &&
                                cd ./news-etl/jars &&
                                curl -u \$NEXUS_USERNAME:\$NEXUS_PASSWORD -O ${env.NEXUS}:8081/repository/3rd-party/jars/tika-core-2.9.0.jar
                            "
                        """
                    }
                }
                echo 'Transferring  to node 3: ${env.NIFI_NODE_3}'
                sshagent(credentials: ['nifi-node-3']) {
                    withCredentials([usernamePassword(credentialsId: 'nexus-login', passwordVariable: 'NEXUS_PASSWORD', usernameVariable: 'NEXUS_USERNAME')]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no user@${env.NIFI_NODE_3} "
                                rm -rf news-etl &&
                                git clone https://github.com/j7breuer/news-etl.git &&
                                mkdir ./news-etl/jars &&
                                cd ./news-etl/jars &&
                                curl -u \$NEXUS_USERNAME:\$NEXUS_PASSWORD -O ${env.NEXUS}:8081/repository/3rd-party/jars/tika-core-2.9.0.jar
                            "
                        """
                    }
                }
                echo '\n==========================\n[END] Repository file transfer complete...\n==========================\n'
            }
        }
    }
}