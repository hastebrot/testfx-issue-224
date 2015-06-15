# testfx-issue-224

~~~
set MAVEN_OPTS=-Djava.awt.headless=true -Dtestfx.robot=glass -Dtestfx.headless=true -Dprism.order=sw
mvn test
~~~

~~~
mvn test -DargLine="-Djava.awt.headless=true -Dtestfx.robot=glass -Dtestfx.headless=true"
~~~
