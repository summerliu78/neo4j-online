#!/bin/bash
source /etc/profile
source ~/.bash_profile

#时间格式 今天
DATE_YYMMHH=$(date '+%Y-%m-%d')
#时间格式 昨天
DATE_YESTERDAY_YYMMHH=$(date -d 'yesterday' '+%Y-%m-%d')
DATE_LOG_FORMAT=$(date '+%Y-%m-%d %H:%M:%S')
#jar包绝对路径
JAR_FILE_PATH="/server/TRdata/RankFileUpdate/jar/Graph-1.0.0-spark161.jar"
#全类名
CLASS="com.yinker.tinyv.action.RunTrustRanK"
#起始路径
ARGS_ROOT_PATH="/server/TRdata/RankFileUpdate/data"
#边关系文件路径
ARGS_CALLS_FILE_PATH="$ARGS_ROOT_PATH/rankNeed/relationship.txt"
#好种子文件路径
ARGS_GOOD_FILE_PATH="$ARGS_ROOT_PATH/rankNeed/whitelist.txt"
#坏种子文件路径
ARGS_BAD_FILE_PATH="$ARGS_ROOT_PATH/rankNeed/blacklist.txt"
#一度信息文件路径
ARGS_ONE_DEGREE_FILE_PATH="$ARGS_ROOT_PATH/rankNeed/onedegree.txt"
#tomcat读取rank文件绝对路径
ARGS_RESULT_FILE_PATH="$ARGS_ROOT_PATH/rankResult/trustrankResult.txt"
#Rank返回值绝对路径 数据更新到这个文件 防止更新失败
ARGS_RESULT_UP_FILE_PATH="$ARGS_ROOT_PATH/rankResult/trustrankResult.txt.bak"
#分数备份文件路径
ARGS_RESULT_BACKUP_DAYS_PATH="$ARGS_ROOT_PATH/rankResult/backup/trustrankResult.txt-$DATE_YESTERDAY_YYMMHH"
#日志绝对路径
LOG_INFO="/server/TRdata/RankFileUpdate/log/${DATE_YYMMHH}.log"

##############
REMOTE_IP_ADDRESS="10.2.20.168"
REMOTE_USER_NAME="liuwei1"
RESULT_FILE_REMOTE_PATH="$ARGS_ROOT_PATH/rankResult/trustrankResult.txt.1"
ONDEGREE_FILE_REMOTE_PATH="$ARGS_ROOT_PATH/rankNeed/onedegree.txt.1"


function log(){

        #日志级别
	log_level="$1"
        #日志属于哪个
        log_type="$2"
        #日志详情
        log_info="$3"
        #打入日志绝对路径
        log_file_path="$4"
        #追加日志到日志文件
        echo "$DATE_LOG_FORMAT [ $log_level ] [ $log_type ] : $log_info" >>$log_file_path

}


#重试次数
num=5
while [ $num -ge 1 ];do
	log "INFO" "Rank Job" "Will be start rank job" "$LOG_INFO"
	#jar包路径 全类名 边关系文件 好种子文件 坏种子文件 rank结果文件
	java -cp $JAR_FILE_PATH $CLASS $ARGS_CALLS_FILE_PATH $ARGS_GOOD_FILE_PATH $ARGS_BAD_FILE_PATH $ARGS_RESULT_UP_FILE_PATH
	run_status=$?
	#算这是第几次重试
	cs=$(echo "5-$num+1" | bc)
	#如果计算失败
	if [ $run_status -ne 0 ];then
		log "ERROR" "Rank Job" "Run rank job Faild, retry time = $cs" "$LOG_INFO"
		#删掉失败产生的文件
		log "ERROR" "Rank Job" "will be delete run faild job data" "$LOG_INFO"
		rm -f $ARGS_RESULT_FILE_PATH
		#计数
		if [ $num == 1 ];then
			echo "执行失败 $(date)"
			log "ERROR" "Rank Job" "Run 5 times done,all job faild,will be exit" "$LOG_INFO"
			exit 1
		fi

		((num--))
		#retry
		continue
	fi
	log "INFO" "Rank Job" "Run job done,success" "$LOG_INFO"
	#执行成功 退出循环
	break
done



#如果老文件不存在
if [ ! -f $ARGS_RESULT_FILE_PATH ];then
	log "INFO" "Rank Job" "first,Backup last time result file" "$LOG_INFO"
	#先备份文件
	\cp $ARGS_RESULT_UP_FILE_PATH $ARGS_RESULT_BACKUP_DAYS_PATH
	log "INFO" "Rank Job" "first,move file to online path" "$LOG_INFO"
	#把新的数据mv成 线上使用文件
	\mv $ARGS_RESULT_UP_FILE_PATH $ARGS_RESULT_FILE_PATH

	#将生成的分数文件scp到另一台机器
	scp $ARGS_RESULT_FILE_PATH $REMOTE_USER_NAME@$REMOTE_IP_ADDRESS:$RESULT_FILE_REMOTE_PATH
	k=$?
	sleep 0.5
	if [ $? -eq 0 ];then
		ssh $REMOTE_USER_NAME@$REMOTE_IP_ADDRESS "\mv $RESULT_FILE_REMOTE_PATH $ARGS_RESULT_FILE_PATH"
	fi
	#将生成的分数文件scp到另一台机器
	scp $ARGS_ONE_DEGREE_FILE_PATH $REMOTE_USER_NAME@$REMOTE_IP_ADDRESS:$ONDEGREE_FILE_REMOTE_PATH
	k=$?
	sleep 0.5
	if [ $? -eq 0 ];then
		ssh $REMOTE_USER_NAME@$REMOTE_IP_ADDRESS "\mv $ONDEGREE_FILE_REMOTE_PATH $ARGS_ONE_DEGREE_FILE_PATH"
	fi

#原文件存在
else
	#本次更新文件内容计数
	num_new_file_lines=$(wc -l $ARGS_RESULT_UP_FILE_PATH | awk '{print $1}')
	#上次更新文件内容计数
	num_old_file_lines=$(wc -l $ARGS_RESULT_FILE_PATH | awk '{print $1}')

	#新文件数据大于等于旧文件
	if [ $num_new_file_lines -ge $num_old_file_lines ];then
		log "INFO" "Rank Job" "old file exist,Backup last time result file" "$LOG_INFO"
	        #先把原有数据mv到 backup dir里边
	        \mv $ARGS_RESULT_FILE_PATH $ARGS_RESULT_BACKUP_DAYS_PATH
		log "INFO" "Rank Job" "old file exist,move file to online path" "$LOG_INFO"
	        #再把新的数据mv成 线上使用文件
	        \mv $ARGS_RESULT_UP_FILE_PATH $ARGS_RESULT_FILE_PATH
		#将生成的分数文件scp到另一台机器
		scp $ARGS_RESULT_FILE_PATH $REMOTE_USER_NAME@$REMOTE_IP_ADDRESS:$RESULT_FILE_REMOTE_PATH
		k=$?
		sleep 0.5
		if [ $? -eq 0 ];then
			ssh $REMOTE_USER_NAME@$REMOTE_IP_ADDRESS "\mv $RESULT_FILE_REMOTE_PATH $ARGS_RESULT_FILE_PATH"
		fi
		#将生成的分数文件scp到另一台机器
		scp $ARGS_ONE_DEGREE_FILE_PATH $REMOTE_USER_NAME@$REMOTE_IP_ADDRESS:$ONDEGREE_FILE_REMOTE_PATH
		k=$?
		sleep 0.5
		if [ $? -eq 0 ];then
			ssh $REMOTE_USER_NAME@$REMOTE_IP_ADDRESS "\mv $ONDEGREE_FILE_REMOTE_PATH $ARGS_ONE_DEGREE_FILE_PATH"
		fi
		else
		log "ERROR" "Rank Job" "old file exist and new file less then old file number,new file less then old file" "$LOG_INFO"
	fi
fi
