#!/usr/bin/env bash

source /etc/profile
#一天前日期格式
ONE_DAYS_AGO_FORMAT=$(date -d '1 days ago' '+%Y-%m-%d')
#now
NOW_DATE_FORMAT=$(date '+%Y%m%d')
#query每小时统计日志
QUERY_LOG_PATH="/server/moni/log/query-$ONE_DAYS_AGO_FORMAT.log"
#queuetask每小时统计日志
QUEUE_TASK_LOG_PATH="/server/moni/log/queue-task-$ONE_DAYS_AGO_FORMAT.log"
#昨天的日志文件
YESTERDAY_LOG_FILE_PATH="/server/tomcat/logs/logRecord.log.$(date -d '1 days ago' '+%Y-%m-%d')"


IP_ADDRESS=$(ip a | grep "inet" | grep global|awk '{print $2}'|cut -d/ -f1)


function mail(){
        #发件人 昵称<邮箱>
		FROM="request-time-report<complex-alert@yinker.com>"
		#收件人
		TO="liuwei1@yinker.com"
		#抄送
		CC="tangmeng@yinker.com,tangwei@yinker.com,hujunjie@yinker.com,weiyunlei@yinke.com,xiongpeng@yinker.com"
		#CC=
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

#query 响应时间统计
query_num=$(grep '.*DataController - query finish' "$YESTERDAY_LOG_FILE_PATH" |    awk -F= '{print $NF}' | egrep -o '[0-9]+'|awk '{num+=$1}END{print NR,num}' | awk '{wc+=$1;num+=$2}END{printf "query result time avg(ms)  :  ""%.4f\n" ,num/wc}')
#queue task 响应时间统计
queue_task_num=$(grep '.*DataController - queueTask finish' $YESTERDAY_LOG_FILE_PATH |awk -F= '{print $NF}' | egrep -o '[0-9]+'|awk '{num+=$1}END{print NR,num}' |awk '{wc+=$1;num+=$2}END{printf "query result time avg(ms)  :  ""%.4f\n" ,num/wc}')
all_request=$(grep '.*DataController - queueTask finish' $YESTERDAY_LOG_FILE_PATH |awk -F= '{print $NF}' | egrep -o     '[0-9]+'|awk '{num+=$1}END{print "Total number of requests  :  "NR}')
#query_num=$(awk '{wc+=$3;num+=$4}END{printf "query result time avg(ms) :""%.4f\n" ,num/wc}' $QUERY_LOG_PATH)
#all_request=$(awk '{num+=$3}END{print "Total number of requests :"num}' $QUERY_LOG_PATH)
#queue_task_num=$(awk '{wc+=$3;num+=$4}END{printf "queue task result time avg(ms) :""%.4f\n" ,num/wc}' $QUEUE_TASK_LOG_PATH)


mail "$IP_ADDRESS:request-avg-time-report-$ONE_DAYS_AGO_FORMAT" "--> $all_request\n--> $query_num\n--> $queue_task_num"
