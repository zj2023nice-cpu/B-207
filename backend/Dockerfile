# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 1. 拷贝 settings.xml
COPY settings.xml /usr/share/maven/ref/settings.xml

# 2. 拷贝 pom.xml
COPY pom.xml .

# 🚀 关键修改：加上 -s 参数，强制指定使用刚才拷贝的阿里云配置
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline

# 3. 拷贝源码
COPY src ./src

# 🚀 关键修改：打包时也加上 -s 参数，以防万一
RUN mvn -s /usr/share/maven/ref/settings.xml package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget curl
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]