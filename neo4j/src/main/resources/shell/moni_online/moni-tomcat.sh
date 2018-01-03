#!/usr/bin/env bash
source /etc/profile

IP_ADDRESS=$(ip a | grep "inet" | grep global|awk '{print $2}'|cut -d/ -f1)
NOE_FORMAT=$(date '+%Y%m%d_%H%M%S')
ONE_HOURS_AGO_FORMAT=$(date -d '1 hours' '+%Y-%m-%d %H')
ONE_MIN_AGO_FORMAT=$(date -d "$(date '+%Y%m%d %H%M') -1 minute " +"%Y-%m-%d %H:%M")
TODAY_FORMAT=$(date '+%Y-%m-%d')
TOMCAT_LOG_FILE_PATH="/server/tomcat/logs/logRecord.log"
TOMCAT_START_SCRIPT_PATH="/server/tomcat-8080/bin"
#query count 日志
QUERY_LOG_PATH="/server/moni/log/query-$TODAY_FORMAT.log"
#quetask count 日志
QUEUE_TASK_LOG_PATH="/server/moni/log/queue-task-$TODAY_FORMAT.log"
#监控日志
MONI_LOG_PATH="/server/moni/log/moni-$TODAY_FORMAT.log"


#邮件
function mail(){
        #发件人 昵称<邮箱>
		FROM="system<complex-alert@yinker.com>"
		#收件人
		TO="liuwei1@yinker.com"
		#抄送
		CC="tangmeng@yinker.com,tangwei@yinker.com,hujunjie@yinker.com,weiyunlei@yinke.com,xiongpeng@yinker.com"
		#"tangmeng@yinker.com"
		#邮件主题
		SUBJECT="$1"
		#附件文件的绝对路径
	    #UUENCODE="$2"
        #正文
        INFO="$2"
        #uuencode 附件的绝对路径 附件名
        #uuencode $UUENaCODE "$log_name")
		(echo -e "To: $TO\nCC: $CC\nFrom: $FROM\nSubject: $SUBJECT\n\n$INFO") | sendmail -t
		#(echo -e "To: $TO\nCC: $CC\nFrom: $FROM\nSubject: $SUBJECT\n\n$INFO";uuencode $UUENaCODE "$log_name") | sendmail -t
}


function moni_fatal(){

    #grep 动作的关键字
    fatal_grep_info="$1"
    #grep的类型
    log_file_path="$2"
    #fatal 文件类型
    fatal_type="$3"

    num=$(grep "$fatal_grep_info" $log_file_path |wc -l)
    if [ $num -ne 0 ];then
        #主题 内容
        #tomcat-fatal-10.2.20.118-20171220_162041 "find <FATAL LOG <mongo>> log"
	echo "---------------------------------------"
        mail "tomcat-fatal-$IP_ADDRESS-$NOE_FORMAT" "FATAL LOG IN $IP_ADDRESS:$TOMCAT_LOG_FILE_PATH -- FATAL TYPE IS [$fatal_type] INFO ->[$fatal_grep_info]"
	echo "---------------------------------------"
        echo "$(date)    faild tomcat-fatal-$IP_ADDRESS-$NOE_FORMAT --> FATAL LOG IN $IP_ADDRESS:$TOMCAT_LOG_FILE_PATH -- FATAL TYPE IS [$fatal_type]" >> $MONI_LOG_PATH
    else
        #mail "tomcat-fatal-$IP_ADDRESS-$NOE_FORMAT" "FATAL LOG IN $IP_ADDRESS:$TOMCAT_LOG_FILE_PATH -- FATAL TYPE IS [$fatal_type]"
        echo "$(date)    success tomcat-fatal-$IP_ADDRESS-$NOE_FORMAT" >> $MONI_LOG_PATH
    fi
}


function check_tomcat_alive_or_else(){

    #服务名称
    server_name="$1"
    #服务端口
    server_port="$2"
    #其他服务关键字
    server_start_key="$3"

    #进程数
    pid_num=$(ps aux | grep "$server_name"| grep "$server_port" | grep "$server_start_key" | wc -l)

    if [ $pid_num -eq 1 ];then
        return 0
    else
        return 1
    fi
}


function moni_tomcat(){
    #服务名称
    server_name="$1"
    #服务端口
    server_port="$2"
    #其他服务关键字
    server_start_key="$3"
    check_tomcat_alive_or_else "$server_name" "$server_port" "$server_start_key"
    if [ $? -eq 1 ];then
               retry_num=5
               while [ $retry_num -ne 0 ];do
		#	echo "will  be   start tomcat 8080">> $MONI_LOG_PATH
                    nohup $TOMCAT_START_SCRIPT_PATH/startup.sh &>/dev/null &
                    sleep 5
		    check_tomcat_alive_or_else "$server_name" "$server_port" "$server_start_key"
	            if [ $? -eq 1 ];then
                           ps aux | grep "$server_name"| grep "$server_port" | grep "$server_start_key"|awk '{print $2}'|xargs kill -9
                           break
                    fi
                    ((retry_num--))
               done
               echo "$(date)    start-tomcat-faild-$IP_ADDRESS-$NOE_FORMAT start retry 5 times faild">> $MONI_LOG_PATH
               mail "start-tomcat-faild-$IP_ADDRESS-$NOE_FORMAT" "start retry 5 times faild"
    else
	       echo "$(date)	success moni $server_name $server_port $server_start_key">> $MONI_LOG_PATH
    fi


}



#############################################################
#####################           #############################
#####################    MAIN   #############################
#####################           #############################
#############################################################

moni_tomcat "tomcat" "8080" "start"

#mogo
moni_fatal "${ONE_MIN_AGO_FORMAT}.*mongo db error" "$TOMCAT_LOG_FILE_PATH" "mongo"

#neo4j
moni_fatal "${ONE_MIN_AGO_FORMAT}.*DataController.*neo4j db error" "$TOMCAT_LOG_FILE_PATH" "neo4j"

