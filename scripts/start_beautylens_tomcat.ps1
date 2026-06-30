$java = 'C:\Program Files\Java\jdk-21\bin\java.exe'
$classpath = 'D:\Lecture\bin\apache-tomcat-11.0.18\bin\bootstrap.jar;D:\Lecture\bin\apache-tomcat-11.0.18\bin\tomcat-juli.jar'

$args = @(
    '--add-opens=java.base/java.lang=ALL-UNNAMED',
    '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED',
    '--add-opens=java.base/java.io=ALL-UNNAMED',
    '--add-opens=java.base/java.util=ALL-UNNAMED',
    '--add-opens=java.base/java.util.concurrent=ALL-UNNAMED',
    '--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED',
    '-Dcatalina.home=D:\Lecture\bin\apache-tomcat-11.0.18',
    '-Dcatalina.base=D:\Lecture\eclipse-server',
    '-Djava.io.tmpdir=D:\Lecture\eclipse-server\temp',
    '-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager',
    '-classpath',
    $classpath,
    'org.apache.catalina.startup.Bootstrap',
    'start'
)

Set-Location 'D:\Lecture\bin\apache-tomcat-11.0.18'
& $java @args *> 'D:\Lecture\eclipse-server\logs\beautylens-direct.log'
