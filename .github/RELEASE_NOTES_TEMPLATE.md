🐛 修复

- 修复HTTP请求没有可用Host/域名时触发NPE，导致后续ByteBuf未释放，出现内存泄漏问题


