#!/usr/bin/env bash

source /etc/profile
IP_ADDRESS=$(ip a | grep "inet" | grep global|awk '{print $2}'|cut -d/ -f1)
NOE_FORMAT=$(date '+%Y%m%d_%H%M%S')
#ONE_HOURS_AGO_FORMAT=$(date -d '1 hours ago' '+%Y-%m-%d %H')
ONE_HOURS_AGO_FORMAT=$(date  '+%Y-%m-%d %H')
TODAY_FORMAT=$(date '+%Y-%m-%d')

TOMCAT_LOG_FILE_PATH="/server/tomcat/logs/logRecord.log"
#WILL
QUERY_LOG_PATH="/server/moni/log/query-$TODAY_FORMAT.log"
#WILL
QUEUE_TASK_LOG_PATH="/server/moni/log/queue-task-$TODAY_FORMAT.log"
#监控日志
MONI_LOG_PATH="/server/moni/log/moni-$TODAY_FORMAT.log"
#邮件

function count_return_time(){
    #检索关键字
    grep_info="$1"
    #tomcat日志文件绝对路径
    log_file_path="$2"
    #输出文件路径
    count_type="$3"
    #
    if [ $count_type == "query" ];then
        grep "$grep_info" $log_file_path|awk -F= '{print $NF}'|egrep -o "[0-9]+"|awk -v var="$ONE_HOURS_AGO_FORMAT" '{num+=$1}END{print var,NR,num}' >> $QUERY_LOG_PATH
    elif [ $count_type == "queueTask" ];then
        grep "$grep_info" $log_file_path|awk -F= '{print $NF}'|egrep -o "[0-9]+"|awk -v var="$ONE_HOURS_AGO_FORMAT" '{num+=$1}END{print var,NR,num}' >> $QUEUE_TASK_LOG_PATH
    fi
}

#统计query条数 入日志
count_return_time "$ONE_HOURS_AGO_FORMAT.*DataController - query finish" "$TOMCAT_LOG_FILE_PATH" "query"
#统计queueTask条数 入日志
count_return_time "$ONE_HOURS_AGO_FORMAT.*DataController - queueTask finish" "$TOMCAT_LOG_FILE_PATH" "queueTask"
