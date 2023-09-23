pipeline {
    agent any

    stages {
        stage('Environment Setup') {
            steps {
                echo '\n=======================\n[START] Initializing...\n=======================\n'
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL} \n"
                echo "Beginning ETL build"
                echo "SSH into server (enter code later)"
                echo '\n=====================\n[END] Initializing...\n=====================\n'
            }
        }
        stage('Transfer files') {
            steps {
                echo '\n============================\n[START] Repository file transfer...\n============================\n'
                echo 'Transferring files from GitHub...(enter code later)'
                echo '\n==========================\n[END] Repository file transfer...\n==========================\n'
            }
        }
        stage('Pull 3rd party jars from Nexus') {
            steps {
                echo '\n=======================\n[START] Nexus file transfer...\n=======================\n'
                echo 'Transferring files from Nexus...(enter code later)'
                echo '\n=====================\n[END] Nexus file transfer...\n=====================\n'
            }
        }
    }
}