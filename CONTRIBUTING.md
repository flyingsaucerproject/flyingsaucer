Any contributions to FlyingSaucer are both welcomed and appreciated.

## How to run tests

    mvn test

## How to build

    mvn install

This puts the `*.jar` files in your local maven repository at: `~/.m2/repository/org/xhtmlrenderer`


## How to release

To make a release, you need to 
1. have a write permission to `org.xhtmlrenderer` group in Maven central repository.
2. have these lines in `~/.m2/settings.xml`:
   ```xml
   <settings>
      <servers>
        <server>
          <id>central</id>
          <username>********</username>
          <password>*****************************************</password>
        </server>
      </servers>
    </settings>
   ```
   
Steps to release version 10.6.0 (for example)
1. Fill the CHANGELOG.md
2. mvn versions:set -DnewVersion=10.6.0  // replaces previous version by "10.6.0" in pom.xml files
3. mvn clean install -Psign.artifacts    // build & sign all `*.jar` files
4. mvn deploy -Psign.artifacts           // upload `*.jar` files to https://central.sonatype.com/
5. git commit -am "Release 10.6.0"
6. git tag v10.6.0
7. git push --tags origin main
8. mvn versions:set -DnewVersion=10.7.0-SNAPSHOT 
9. git commit -am "Working on 10.7.0"
10. git push origin main
11. Login to https://central.sonatype.com/ 
    * Click "Release" (no need to fill description)
    * After ~5 minutes, the new jar will be available in Central Maven repo
12. Open https://github.com/flyingsaucerproject/flyingsaucer/milestone -> 10.6.0 -> "Edit milestone" -> "Close milestone"
13. Open https://github.com/flyingsaucerproject/flyingsaucer/releases -> "Draft a new release"
    * fill the release details (copy-paste from CHANGELOG.md)
    * click "Publish release"
