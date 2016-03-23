package com.cmz.zookeeper;

public interface Constant {
	 
	//必须先在zookeeper上创建节点/web_service
    String ZK_CONNECTION_STRING = "host1:2181,host2:2181,host3:2181";
    int ZK_SESSION_TIMEOUT = 5000;
    String ZK_REGISTRY_PATH = "/web_service";
    String ZK_PROVIDER_PATH = ZK_REGISTRY_PATH + "/provider";
}
