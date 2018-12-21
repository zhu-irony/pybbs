package co.yiiu.pybbs.config.service;

import co.yiiu.pybbs.model.SystemConfig;
import co.yiiu.pybbs.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by tomoya.
 * Copyright (c) 2018, All Rights Reserved.
 * https://yiiu.co
 */
@Component
public class RedisService implements BaseService<JedisPool> {

  @Autowired
  private SystemConfigService systemConfigService;
  private JedisPool jedisPool;
  private Logger log = LoggerFactory.getLogger(RedisService.class);

  public void setJedis(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  @Override
  public JedisPool instance() {
    try {
      if (this.jedisPool != null) return this.jedisPool;
      // 获取redis的连接
      // host
      SystemConfig systemConfigHost = systemConfigService.selectByKey("redis.host");
      String host = systemConfigHost.getValue();
      // port
      SystemConfig systemConfigPort = systemConfigService.selectByKey("redis.port");
      String port = systemConfigPort.getValue();
      // password
      SystemConfig systemConfigPassword = systemConfigService.selectByKey("redis.password");
      String password = systemConfigPassword.getValue();
      password = StringUtils.isEmpty(password) ? null : password;
      // database
      SystemConfig systemConfigDatabase = systemConfigService.selectByKey("redis.database");
      String database = systemConfigDatabase.getValue();
      // timeout
      SystemConfig systemConfigTimeout = systemConfigService.selectByKey("redis.timeout");
      String timeout = systemConfigTimeout.getValue();
      // ssl
      SystemConfig systemConfigSSL = systemConfigService.selectByKey("redis.ssl");
      String ssl = systemConfigSSL.getValue();

      if (StringUtils.isEmpty(host)
          || StringUtils.isEmpty(port)
          || StringUtils.isEmpty(database)
          || StringUtils.isEmpty(timeout)) {
        log.info("redis配置信息不全或没有配置...");
        return null;
      }
      JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
      // 配置jedis连接池最多空闲多少个实例，源码默认 8
      jedisPoolConfig.setMaxIdle(8);
      // 配置jedis连接池最多创建多少个实例，源码默认 8
      jedisPoolConfig.setMaxTotal(18);
      jedisPool = new JedisPool(
          jedisPoolConfig,
          host,
          Integer.parseInt(port),
          Integer.parseInt(timeout),
          password,
          Integer.parseInt(database),
          null,
          ssl.equals("1")
      );
      log.info("redis连接对象获取成功...");
      return this.jedisPool;
    } catch (Exception e) {
      log.error("配置redis连接池报错，错误信息: {}", e.getMessage());
      return null;
    }
  }

  // 获取String值
  public String getString(String key) {
    JedisPool instance = this.instance();
    if (StringUtils.isEmpty(key) || instance == null) return null;
    return instance.getResource().get(key);
  }

  public void setString(String key, String value) {
    JedisPool instance = this.instance();
    if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value) || instance == null) return;
    instance.getResource().set(key, value); // 返回值成功是 OK
  }

  public void delString(String key) {
    JedisPool instance = this.instance();
    if (StringUtils.isEmpty(key) || instance == null) return;
    instance.getResource().del(key); // 返回值成功是 1
  }

  // TODO 后面会补充获取 list, map 等方法

}