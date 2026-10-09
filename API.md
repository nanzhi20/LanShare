# LanShare 接口文档

服务默认运行在 `http://localhost:8080`。同一局域网中的设备可将 `localhost` 换成电脑的局域网 IPv4 地址。所有路径均以该地址为前缀；当前版本没有身份验证，仅适合可信局域网，勿暴露到公网。

文件和文字接口均未限制设备。任意能够访问服务的设备都可以调用新增、查询和删除接口。

## 文字对象

新增文字成功或查询文字列表时，记录结构为：

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "content": "需要共享的文字",
  "createdAt": "2026-10-09T08:30:00Z"
}
```

`id` 是记录 UUID；`content` 保留原始换行和首尾空格；`createdAt` 是 ISO 8601 UTC 时间。

## 文字接口

### 新增文字

`POST /api/texts`

请求类型为 `application/json`：

```json
{ "content": "需要共享的文字" }
```

内容不能全部为空白，且不能超过 20,000 个字符。成功返回 `201 Created` 和文字对象；内容无效时返回 `400`。

```powershell
curl.exe -X POST -H "Content-Type: application/json" -d '{"content":"来自电脑的文字"}' http://localhost:8080/api/texts
```

### 查询文字列表

`GET /api/texts`

成功返回 `200 OK` 和文字对象数组，按创建时间从新到旧排列；没有记录时返回 `[]`。

### 删除文字

`DELETE /api/texts/{id}`

成功返回 `204 No Content`，无响应体。删除会对所有设备生效且不可恢复。ID 无效或记录不存在时返回 `404`。

## 文件对象

上传成功或查询列表时，文件记录的结构为：

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "example.txt",
  "size": 1234,
  "uploadedAt": "2026-10-09T08:30:00Z"
}
```

| 字段 | 含义 |
| --- | --- |
| `id` | 文件的 UUID；下载、删除时使用此值，不使用文件名。 |
| `name` | 上传时的原文件名；同名文件会分别保存并拥有不同的 ID。 |
| `size` | 文件大小，单位为字节。 |
| `uploadedAt` | ISO 8601 UTC 时间。当前实现取已保存文件的最后修改时间，表示上传时间。 |

## 文件接口

### 上传文件

`POST /api/files`

请求类型为 `multipart/form-data`，文件字段名必须为 `file`。单文件默认上限为 100 MB；空文件和无效文件名会被拒绝。成功返回 `201 Created`，响应体是上述文件对象。

```powershell
curl.exe -F "file=@C:\path\to\example.txt" http://localhost:8080/api/files
```

可能返回 `400`（缺少文件、空文件或文件名无效）、`413`（文件超过上限）、`500`（保存失败）。文件名不得包含路径分隔符和不安全字符，也不能是 Windows 保留名。

### 查询文件列表

`GET /api/files`

成功返回 `200 OK` 和文件对象数组，按 `uploadedAt` 从新到旧排列；没有文件时返回 `[]`。

```powershell
curl.exe http://localhost:8080/api/files
```

读取存储目录失败时返回 `500`。

### 下载文件

`GET /api/files/{id}/download`

将 `{id}` 替换为列表中的 UUID。成功返回 `200 OK`、文件内容（`application/octet-stream`）及带原文件名的 `Content-Disposition: attachment` 响应头。

```powershell
curl.exe -OJ http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000/download
```

ID 无效或文件不存在时返回 `404`；读取文件失败时返回 `500`。

### 删除文件

`DELETE /api/files/{id}`

将 `{id}` 替换为列表中的 UUID。成功返回 `204 No Content`，无响应体。删除后文件不可恢复；ID 无效或文件不存在时返回 `404`，删除失败时返回 `500`。

```powershell
curl.exe -X DELETE http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000
```

## 页面配置与网络地址

### 查询文件传输配置

`GET /api/files/config`

成功返回 `200 OK`：

```json
{
  "maxFileSizeBytes": 104857600,
  "storageDirectory": "C:\\path\\to\\LanShare\\uploads"
}
```

`maxFileSizeBytes` 是服务端单文件上限的字节数；`storageDirectory` 是当前运行环境中的绝对存储路径。默认配置为 `spring.servlet.multipart.max-file-size=100MB`、`spring.servlet.multipart.max-request-size=101MB`、`lanshare.storage-dir=uploads`。相对存储路径以应用运行目录为基准。

### 查询局域网地址

`GET /api/network/addresses`

成功返回 `200 OK` 和当前活动网卡的私有 IPv4 地址数组，用于在页面展示可尝试访问的电脑地址；没有符合条件的地址时返回 `[]`。单项示例：

```json
{
  "interfaceName": "Wi-Fi",
  "ip": "192.168.1.20"
}
```

`interfaceName` 是网卡名称，`ip` 是对应 IPv4 地址。返回地址不保证手机一定可达，还取决于手机所在网络及电脑防火墙设置。

### 生成局域网访问二维码

`GET /api/network/qr?ip={ip}`

`ip` 必须是“查询局域网地址”接口当前返回的地址。服务使用当前请求的协议和端口拼出完整首页 URL，并在本机生成二维码。成功返回 `200 OK` 和 `image/png` 图片，响应带有 `Cache-Control: no-store`。

```powershell
curl.exe "http://localhost:8080/api/network/qr?ip=192.168.1.20" --output address.png
```

二维码只编码访问 URL，不会建立网络连接或绕过防火墙。地址不属于当前活动网卡时返回 `400`；生成图片失败时返回 `500`。本功能使用本地 ZXing 依赖，不调用第三方二维码服务。

## 错误响应

上述接口由应用处理的错误使用以下 JSON 结构：

```json
{ "error": "文件不存在" }
```

| 状态码 | 常见原因 |
| --- | --- |
| `400 Bad Request` | 文件上传无效，或共享文字为空白、超过长度限制。 |
| `404 Not Found` | 文件或文字记录的 ID 无效，或对应记录不存在。 |
| `413 Payload Too Large` | 上传文件超过服务端上限。 |
| `500 Internal Server Error` | 文件或文字记录的保存、读取、删除失败。 |

这里列出的是当前应用明确处理的情况；其他框架层错误不保证使用相同的 JSON 结构。
