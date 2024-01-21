Any contributions to FlyingSaucer are both welcomed and appreciated.

## How to run tests

    mvn test

## How to build

    mvn install

This puts the `*.jar` files in your local maven repository at: `~/.m2/repository/org/xhtmlrenderer`


## How to release

To make a release, you need to 
1. have a write permission to `org.xhtmlrenderer` group in Maven central repository.
2. have these lines in `~/.gradle/gradle.properties`:
   > signing.keyId=2#####8
   > signing.password=***********************
   > signing.secretKeyRingFile=/Users/andrei/.gnupg/secring.gpg
   > sonatypeUsername=*******
   > sonatypePassword=********************
   
Steps to release version 9.5.0 (for example)
1. Fill the CHANGELOG.md
2. mvn versions:set -DnewVersion=9.5.0   // replaces previous version by "9.5.0" in pom.xml files
3. git commit -am "Release 9.5.0"
4. git tag v9.5.0 
5. git push --tags origin main
6. mvn clean deploy   // uploads the `*.jar` files to https://oss.sonatype.org
7. mvn versions:set -DnewVersion=9.5.1-SNAPSHOT 
8. git commit -am "Working on 9.5.1"
9. git push origin main

10. Login to https://oss.sonatype.org/#stagingRepositories 
    * Click "Release" (no need to fill description)
    * After ~5 minutes, the new jar will be available in Central Maven repo
11. Open https://github.com/flyingsaucerproject/flyingsaucer/milestone -> 9.5.0 -> "Edit milestone" -> "Close milestone"
12. Open https://github.com/flyingsaucerproject/flyingsaucer/releases -> "Draft a new release"
    * fill the release details (copy-paste from CHANGELOG.md)
    * click "Publish release"
