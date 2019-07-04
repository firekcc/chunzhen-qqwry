# chunzhen-qqwry
the chunzhen ip database 

纯真网络 cz88.net

&nbsp;

- dat文件更新脚本
```
1.cd ${MYWORKSPACE}/src/main/resources && pwd
2.docker run -it --rm --name my-running-script -v "$PWD":/usr/src/myapp -w /usr/src/myapp python:alpine python update_chunzhen.py qqwry.dat
```

&nbsp;

- 代码使用示例
```
    private static final String IP_REGEXP = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

    private static boolean checkIp(String item) {
        if (StringUtils.isNotEmpty(item)) {
            Pattern pattern = Pattern.compile(IP_REGEXP);
            Matcher matcher = pattern.matcher(item);
            return matcher.find();
        }
        return false;
    }
    
    public static IPZone getIPZone(String ip) {
        if (!checkIp(ip)) {
            return null;
        }
        IPZone ipzone = null;
        try {
            ipzone = new QQWry().findIP(ip);
        } catch (IOException e) {
            logger.error("IPZone IO-ERROR ", e);
        } catch (IllegalArgumentException e) {
            logger.error("IPZone IllegalArgument-ERROR ", e);
        } catch (Exception e) {
            logger.error("IPZone ERROR ", e);
        }
        return ipzone;
    }
    
    Optional<String> ipCity = Optional.ofNullable(getIPZone(ip));
        
```