 node('maven') {
  //env.PATH = "${tool 'Maven 3'}/bin:${env.PATH}"
  stage 'Checkout'
  //checkout scm
  git url: "https://github.com/thexman/ldap-um.git"

  stage 'Build'
  sh 'mvn clean package'
 }
