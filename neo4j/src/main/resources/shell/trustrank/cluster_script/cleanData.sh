#!/bin/bash


#保存天数
SAVE_SDAYS=15
#hdfs数据目录
HDFS_PATH=
#hdfs down下来的数据目录
HDFS_LOCAL_PATH='/server/tinyv_data/lw/blackWhriteKList/data'
#日志文件数据
APP_LOG_PATH='/server/tinyv_data/lw/blackWhriteKList/logs/app_log'
#spark client 运行时日志数据
SPARK_LOG_PATH='/server/tinyv_data/lw/blackWhriteKList/logs/spark_log'


#清理hdfs本地文件
find $HDFS_LOCAL_PATH/ -maxdepth 1 -type d -mtime +$SAVE_SDAYS |xargs rm -rf
#清理本地日志
find $APP_LOG_PATH/ -type f -mtime +$SAVE_SDAYS |xargs rm -rf
#清理spark日志
find $SPARK_LOG_PATH/ -type d -mtime +$SAVE_SDAYS | xargs rm -rf
