# LanShare

LanShare 是一个基于 Spring Boot 的局域网文件共享工具。第一版支持手机通过浏览器上传文件，电脑查看、下载和手动删除文件。

## 运行方法

需要 Java 17 和 Maven。在项目根目录运行 `mvn spring-boot:run`，或在 IDEA 中运行 `LanShareApplication`。电脑浏览器打开 `http://localhost:8080`，首页会列出当前电脑的局域网 IPv4 地址。手机与电脑连接同一局域网后，在手机浏览器打开页面显示的 Wi-Fi 地址。如果无法连接，检查电脑防火墙是否允许本应用在当前网络接收连接。

上传文件保存在项目运行目录的 `uploads/`，首页会显示实际的绝对保存路径。服务重启后文件仍会保留；该目录不会提交到 Git。上传上限由 `application.properties` 中的 `spring.servlet.multipart.max-file-size` 决定，默认单文件 100 MB；如需修改上限，也要相应调整 `spring.servlet.multipart.max-request-size`。相同文件名的上传会分别保存。

## 使用范围

当前版本不设访问码，仅适合可信的局域网。知道服务地址的同网设备可以访问文件列表、上传、下载和删除文件。不要将服务端口暴露到公网。

## 手动验收

在 IDEA 中可以分别运行 `NetworkAddressManualCheck.main()` 和 `FileStorageManualCheck.main()`，查看网卡地址与文件存储逻辑的输出。这两个入口位于 `src/test/java`，不会随应用启动自动运行；文件存储检查使用独立临时目录，不影响正式的 `uploads/`。

1. 在电脑上启动服务，打开 `http://localhost:8080`；在同一 Wi-Fi 下的手机上打开电脑的局域网地址。
2. 用手机上传一个普通文件，电脑刷新页面后应看到文件名、大小和上传时间，并能下载到与原文件内容一致的文件。
3. 再上传一个同名文件，列表中应出现两条互不覆盖的记录。
4. 重启服务，确认两条记录仍在；删除其中一条，另一条应保留。
5. 尝试空文件和超过 100 MB 的文件，应看到错误提示；访问已删除文件的下载地址应返回 404。
6. 用 `git status` 检查，上传的文件及 `target/` 不应出现在待提交文件中。

## 后续计划

- 局域网文字共享
