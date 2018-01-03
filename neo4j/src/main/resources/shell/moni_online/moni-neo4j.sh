#!/usr/bin/env bash
source /etc/profile


IP_ADDRESS=$(ip a | grep "inet" | grep global|awk '{print $2}'|cut -d/ -f1)
NOE_FORMAT=$(date '+%Y%m%d_%H%M%S')
ONE_HOURS_AGO_FORMAT=$(date -d '1 hours' '+%Y-%m-%d %H')
ONE_MIN_AGO_FORMAT=$(date -d "$(date '+%Y%m%d %H%M') -1 minute " +"%Y-%m-%d %H:%M")
TODAY_FORMAT=$(date '+%Y-%m-%d')
NEO4J_START_SCRIPT_PATH="/server/neo4j/bin"

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




function check_neo4j_alive_or_else(){

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


function moni_neo4j(){
    #服务名称
    server_name="$1"
    #服务端口
    server_port="$2"
    #其他服务关键字
    server_start_key="$3"
    check_neo4j_alive_or_else "$server_name" "$server_port" "$server_start_key"
    if [ $? -eq 1 ];then
               retry_num=5
               while [ $retry_num -ne 0 ];do
                    $NEO4J_START_SCRIPT_PATH/neo4j  start &>/dev/null
                    sleep 5
    		    check_neo4j_alive_or_else "$server_name" "$server_port" "$server_start_key"
		    if [ $? -eq 1 ];then
                           ps aux | grep "$server_name"| grep "$server_port" | grep "$server_start_key"|awk '{print $2}'|xargs kill -9
                           break
                    fi
                    ((retry_num--))
               done
        	mail "start-neo4j-faild-$IP_ADDRESS-$NOE_FORMAT" "start retry 5 times faild"
	        echo "$(date)	start-neo4j-faild-$IP_ADDRESS-$NOE_FORMAT start retry 5 times faild">> $MONI_LOG_PATH
    else
		echo "$(date)   success moni service $server_name $server_port $server_start_key"
    fi


}



#############################################################
#####################           #############################
#####################    MAIN   #############################
#####################           #############################
#############################################################

moni_neo4j "neo4j" "juece" "encoding=UTF-8"