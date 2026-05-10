pipeline {
    agent any

    environment {
        DOCKER_IMAGE       = 'goesbernardo/signflow'
        DOCKER_TAG         = "${BUILD_NUMBER}"
        // Render deploy hook — cadastrar no Jenkins antes de usar:
        // Manage Jenkins → Credentials → Global → Add → Secret text
        // ID: render-deploy-hook-url
        RENDER_DEPLOY_HOOK = credentials('render-deploy-hook-url')

        // Garante que Docker seja encontrado independente da instalação
        PATH = "/usr/local/bin:/usr/bin:/bin:${env.PATH}"
    }

    stages {

        // ── Checkout ──────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME} | Build: #${BUILD_NUMBER}"
            }
        }

        // ── Diagnóstico ───────────────────────────────────────────
        stage('Diagnóstico') {
            steps {
                sh '''
                    echo "=== Java ==="
                    java -version

                    echo "=== Maven Wrapper ==="
                    chmod +x ./mvnw
                    ./mvnw --version

                    echo "=== Docker ==="
                    which docker || echo "docker nao encontrado no PATH"
                    docker --version || echo "docker nao disponivel"

                    echo "=== PATH atual ==="
                    echo $PATH

                    echo "=== Usuario Jenkins ==="
                    whoami && id
                '''
            }
        }

        // ── Build ─────────────────────────────────────────────────
        stage('Build') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        // ── Testes ────────────────────────────────────────────────
        stage('Test') {
            steps {
                sh './mvnw test'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        // ── Docker Build ──────────────────────────────────────────
        stage('Docker Build') {
            steps {
                sh '''
                    echo "Construindo imagem Docker..."
                    docker build \
                        -t $DOCKER_IMAGE:$DOCKER_TAG \
                        -t $DOCKER_IMAGE:latest \
                        .
                    echo "Imagem construida: $DOCKER_IMAGE:$DOCKER_TAG"
                '''
            }
        }

        // ── Docker Push ───────────────────────────────────────────
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo "Autenticando no Docker Hub..."
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin

                        docker push $DOCKER_IMAGE:$DOCKER_TAG
                        docker push $DOCKER_IMAGE:latest

                        echo "Push concluido: $DOCKER_IMAGE:$DOCKER_TAG"
                        docker logout
                    '''
                }
            }
        }

        // ── Deploy Render (somente branch main) ───────────────────
        stage('Deploy Render') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    echo "Acionando deploy no Render..."
                    curl -X POST "$RENDER_DEPLOY_HOOK" \
                         -H "Content-Type: application/json" \
                         --fail \
                         --silent \
                         --show-error
                    echo "Deploy acionado com sucesso."
                '''
            }
        }
    }

    // ── Pós-execução ──────────────────────────────────────────────
    // IMPORTANTE: sh e cleanWs precisam de contexto de node.
    // Usar node('') garante que sempre haverá contexto disponível.
    post {
        success {
            echo "✅ Pipeline concluído — imagem: $DOCKER_IMAGE:$DOCKER_TAG"
        }
        failure {
            echo "❌ Pipeline falhou no build #${BUILD_NUMBER}"
        }
        always {
            node('') {
                cleanWs()
            }
        }
    }
}