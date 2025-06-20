# Transaction API 性能测试指南

## 前提条件

1. 安装 JMeter 5.6.2 或更高版本
2. 确保 Transaction API 服务已经启动并运行
3. 确保测试环境与生产环境配置相似

## 测试配置说明

测试计划包含以下主要配置：

- 线程组配置：
  - 并发用户数：500
  - 启动时间：10秒
  - 循环次数：1

- 测试接口：
  - 创建交易 (POST /api/v1/transactions)
  - 查询所有交易 (GET /api/v1/transactions/all)

## 运行测试

1. 打开 JMeter
2. 加载 `stress-test.jmx` 文件
3. 在 HTTP Request Defaults 中设置服务器地址和端口
4. 点击"开始"按钮运行测试

## 测试结果

测试结果将保存在 `stress_test_results.jtl` 文件中，包含以下指标：

- 响应时间
- 吞吐量
- 错误率
- 并发用户数
- 请求成功率

## 性能指标参考

建议的性能指标：

- 平均响应时间：< 200ms
- 95%响应时间：< 500ms
- 错误率：< 1%
- 吞吐量：> 100 TPS

## 注意事项

1. 建议在非生产环境进行测试
2. 测试前确保数据库有足够的测试数据
3. 监控服务器资源使用情况
4. 根据实际需求调整并发用户数和测试时长 