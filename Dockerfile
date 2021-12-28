FROM firekcc/java

MAINTAINER firekcc<lyk_1226@126.com>

# ARG VERSION=0.0.1-SNAPSHOT

RUN mkdir -p /usr/local/chunzhen-qqwry

ADD ./build/libs/chunzhen-qqwry.jar /usr/local/chunzhen-qqwry/

CMD java -jar -Xmx512m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m -Duser.timezone=GMT+08 /usr/local/chunzhen-qqwry/chunzhen-qqwry.jar
