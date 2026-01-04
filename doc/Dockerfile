# 在docker容器中运行native程序
from debian:stable-slim
ADD ./target/etp-server /usr/local/etp/etps
CMD ["/usr/local/etp/etps"]
expose 9527

# docker build -t xiaoniucode/etps:1.0.0 .

