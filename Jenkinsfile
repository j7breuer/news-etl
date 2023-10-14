pipeline {
    agent any

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
                            echo "Checking who I am"
                            whoami
                            echo "Checking my environment"
                            env
                            ssh -o StrictHostKeyChecking=no user@${env.NIFI_NODE_1} "
                                git clone https://github.com/j7breuer/news-etl.git &&
                                mkdir ./news-etl/jars &&
                                cd ./news-etl/jars &&
                                curl -u \$NEXUS_USERNAME:\$NEXUS_PASSWORD -O ${env.NEXUS}:8081/repository/3rd-party/jars/tika-core-2.9.0.jar
                            "
                        """
                    }
                }
                echo 'Transferring  to node 2: ${env.NIFI_NODE_2}'
                sshagent(credentials: ['nifi-node-2']) {
                    withCredentials([usernamePassword(credentialsId: 'nexus-login', passwordVariable: 'NEXUS_PASSWORD', usernameVariable: 'NEXUS_USERNAME')]) {
                        sh '''
                            ssh user@${env.NIFI_NODE_2} "git clone https://github.com/j7breuer/news-etl.git && mkdir ./news-etl/jars && cd ./news-etl/jars"
                            ssh user@${env.NIFI_NODE_2} "curl -u $NEXUS_USERNAME:$NEXUS_PASSWORD -o ${env.NEXUS}:8081/repository/3rd-party/jars/tika-core-2.9.0.jar"
                        '''
                    }
                }
                echo 'Transferring  to node 3: ${env.NIFI_NODE_3}'
                sshagent(credentials: ['nifi-node-3']) {
                    withCredentials([usernamePassword(credentialsId: 'nexus-login', passwordVariable: 'NEXUS_PASSWORD', usernameVariable: 'NEXUS_USERNAME')]) {
                        sh '''
                            ssh user@${env.NIFI_NODE_3} "git clone https://github.com/j7breuer/news-etl.git && mkdir ./news-etl/jars && cd ./news-etl/jars"
                            ssh user@${env.NIFI_NODE_3} "curl -u $NEXUS_USERNAME:$NEXUS_PASSWORD -o ${env.NEXUS}:8081/repository/3rd-party/jars/tika-core-2.9.0.jar"
                        '''
                    }
                }
                echo '\n==========================\n[END] Repository file transfer complete...\n==========================\n'
            }
        }
    }
}