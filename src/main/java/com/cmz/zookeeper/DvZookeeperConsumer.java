package com.cmz.zookeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DvZookeeperConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(DvZookeeperConsumer.class);
    // 用于等待 SyncConnected 事件触发后继续执行当前线程
    private CountDownLatch latch = new CountDownLatch(1);
    private volatile List<String> urlList = new ArrayList<>();
    
    public DvZookeeperConsumer() {
		ZooKeeper zKeeper = connectServer();
		if(zKeeper!=null){
			watchNode(zKeeper);
		}
	}
    
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
			zk = new ZooKeeper(Constant.ZK_CONNECTION_STRING, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected){
						latch.countDown();
					}
				}
			});
			latch.await(); // 使当前线程处于等待状态
		} catch (IOException | InterruptedException e) {
			LOGGER.error("",e.getStackTrace());
		}
        return zk;
    }
    
    
    // 观察 /registry 节点下所有子节点是否有变化
    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk); // 若子节点有变化，则重新调用该方法（为了获取最新子节点中的数据）
                    }
                }
            });
            List<String> dataList = new ArrayList<>(); // 用于存放 /registry 所有子节点中的数据
            for (String node : nodeList) {
                byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null); // 获取 /registry 的子节点中的数据
                dataList.add(new String(data));
            }
            LOGGER.info("node data: {}", dataList);
            urlList = dataList; // 更新最新的
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }
    
}
