Goal
=


The syslog-ng application is a flexible and highly scalable system logging application that is ideal for creating centralized and trusted logging solutions. And Java is one of the most popularity programming language nowadays. Although the syslog-ng has a possibility to write destinations in Java, it's far from enough. The goal of this project is to extend this functionality, and make it possible to write filters, parsers, rewrite rules, template functions, and even sources in Java. 

Benefits
===
### For syslog-ng

Syslog-ng is powerful mostly for its flexible configuration. In [The syslog-ng Open Source Edition 3.6 Administrator Guide](http://www.balabit.com/sites/default/files/documents/syslog-ng-ose-latest-guides/en/syslog-ng-ose-guide-admin/html/index.html), a demo configuration to receive and parse log from Tomcat is provided. But it's truly more convenient if the user can write source and parse in java. By this way, it's also possible to write a highly structured message. On the other hand, a java programmer can easily extent the functionality of syslog-ng by using java APIs. And that feature can make great use of existing java library, like string parsing, socket communication, file operation and so on. 

In all, this project will improve syslog-ng's functionality by providing flexible api for java, and thus make it take advantage of so many existing java libraries, as well as features provided by java.


### For me

To be honest, I am a java programmer because the encapsulation that java provides helps a lot when developing software. But I never think a programmer using high level language like java can ignore how it's implemented in the low level. So I always concern for these low level details behind java. For example, when using java nio, I first google selector and epoll in c. And in my mind, a good java programmer must also be good at c. So I am really interested in this project, the binding of c and java is just what I want to learn. This project should give me a great chance to gain project experience in both c and java. And with jni, I can also get a better understanding on java.

Details
=

### Structure in language level

The task of this project can be briefly split into two parts: java and c. Java is responsible of providing high level interface or API to concrete java code wirten by users. On the other hand, c is supposed to play the role of an interpreter, making it possible for java code to communicate with syslog-ng core. And it's obvious that c coding should take up most of the time.

Java files are compiled and packaged into SyslogNg.jar, then deployed to syslog-ng module directory. And c files are compiled into both static and dynamic library file, then deployed to syslog-ng module directory too. Both of them will be loaded by system-ng when it's started. The work of this project is none of official repository, but is a module for syslog-ng, as described in [syslog-ng-incubator home page](https://github.com/balabit/syslog-ng-incubator).

More technically, java create native methods and c implements them using jni. These c codes are called proxy in structure, meaning that they only transfer data from java code to syslog-ng core or the other direction. But to complete these proxies, it's essential to have a global view over syslog-ng and know which function or variable to use when delivering 'message'.

### Structure in functionality level

Syslog-ng provides flexible configuration options and what I care in this project are destination, filter, parser, rule, and template function. As described before, destination functionality has already been implemented, so the rest are most of my work, and they should all follow the design pattern of destination. 



Timeline
=

 - May 10th - May 24th
	 - Preparation, autoconfig tools, figure out how to modify configure files according to added files.
	 - Preparation, base structure of syslog-ng, find out how proxy code is triggered. Possibly analyze from destination code.
 - May 25th - June 10th
	 - Design, create java class and methods to meet requirement of the functionality. Some demo user java code may also be required.
	 - Design, c source file and function for every functionality, mainly proxy files and jni function related to java native method
 - June 11st - July 10th
	 - Coding, start from filter, as suggested by Juhász. This part is truly the start of major parts of the project and every single function should be tested. So unit test code should be created here.
 - July 11th - July 21st
	 - Coding, parser and rule functionality, also numerous test cases are required and all of them should be kept.
 - July 22th - July 31st
	 - Coding, template functionality. The requirement is the same as previous coding. Besides, if time permits I can try to implement source functionality, but it's optional.
 - Aug 1st - Aug 12th
	 - Integration Test, some documentation if needed
	 - Review, get feedback from mentor and improve design in global view
 - Aug 13th - Aug 20th
	 - Cleaning, clear unnecessary code, mainly annotation and debug message
	 - Summary, last version committed

About me
=

 > **Contacting Info:**
 
 > - Name(Chinese): 李学兵
 > - Name(English): Johnson Li
 > - Email: 12307130211@fudan.edu.cn, johnsonli1993@gmail.com

I'm a third year b.s. student at Fudan University, School of Computer Science and I'm good at c and Java. In operating system class, I completed [ucore lab](https://github.com/chyyuu/mooc_os_lab)  independently, which requires students to complete part of linux kernel based on version 2.4. That proves my ability in c. As to java, I gained much experience in software development during my internship. My work experience is related to Continuous Integration, Continuous Delivery, web development based on spring and so on. Besides, as a linux user, I'm familiar with shell script as well as some useful tools, including syslog-ng. 

It's the first time that I work for open source community. But since the first time that I know coding, I have used numerous open source projects. And I's really exciting if I can contribute to syslog-ng.

Additional Information
=
My solution to the simple task that Juhász proposed: <https://github.com/johnson-li/syslog-ng-incubator>
