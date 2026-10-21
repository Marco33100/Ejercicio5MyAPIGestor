pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
    }

    environment {
        DB_HOST = 'mysql-java21-ci'
        DB_PORT = '3306'
        DB_NAME = 'dbgestor_ci'
    }

    stages {
        stage('Descargar proyecto') {
            steps {
                withEnv([
                    'GIT_CONFIG_COUNT=1',
                    'GIT_CONFIG_KEY_0=http.version',
                    'GIT_CONFIG_VALUE_0=HTTP/1.1'
                ]) {
                    retry(2) {
                        checkout scmGit(
                            branches: [[name: '*/main']],
                            extensions: [
                                cloneOption(
                                    shallow: true,
                                    depth: 1,
                                    noTags: true,
                                    honorRefspec: true,
                                    timeout: 30
                                )
                            ],
                            userRemoteConfigs: [[
                                url: 'https://github.com/Marco33100/Ejercicio5MyAPIGestor.git',
                                refspec: '+refs/heads/main:refs/remotes/origin/main'
                            ]]
                        )
                    }
                }
            }
        }

        stage('Ver Directorio') {
            steps {
                sh 'pwd'
                sh 'ls -la'
                sh 'cat pom.xml'
            }
        }

        stage('Verificar Java y Maven') {
            steps {
                sh 'java -version'
                sh 'javac -version'
                sh 'mvn -version'
            }
        }

        stage('Build & Test') {
            steps {
                dir('.') {
                    withCredentials([usernamePassword(
                        credentialsId: 'mysql-ci',
                        usernameVariable: 'DB_USER',
                        passwordVariable: 'DB_PASSWORD'
                    )]) {
                        sh '''
                            mvn --batch-mode --no-transfer-progress clean verify -Dsurefire.useFile=false
                        '''
                    }
                }
            }
        }

        stage('Publicar cobertura') {
            steps {
                recordCoverage(
                    tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                    sourceDirectories: [[path: 'src/main/java']],
                    sourceCodeRetention: 'LAST_BUILD',
                    failOnError: true
                )
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarServer') {
                    dir('.') {
                        sh '''
                            set +x
                            SONAR_TOKEN="$SONAR_AUTH_TOKEN" \
                            mvn --batch-mode --no-transfer-progress \
                                org.sonarsource.scanner.maven:sonar-maven-plugin:5.5.0.6356:sonar \
                                -Dsonar.projectKey=Ejercicio5MyAPIGestor \
                                -Dsonar.projectName=Ejercicio5MyAPIGestor \
                                -Dsonar.host.url=http://host.docker.internal:9000 \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }

        stage('Security Scan (SAST) - Semgrep') {
            steps {
                dir('.') {
                    sh '''
                        mkdir -p reportes
                        rm -f reportes/semgrep.json
                        "$JENKINS_HOME/tools/semgrep-1.181.0/bin/semgrep" scan \
                            --config=auto \
                            --json \
                            --output=reportes/semgrep.json \
                            .
                    '''
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'reportes/semgrep.json',
                        allowEmptyArchive: true,
                        fingerprint: true
                }
            }
        }
    }

    post {
        always {
            script {
                if (fileExists('target/surefire-reports')) {
                    junit testResults: 'target/surefire-reports/*.xml'
                }
            }
        }
    }
}
