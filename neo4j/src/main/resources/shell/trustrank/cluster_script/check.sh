#!/bin/bash


source /etc/profile
source ~/.bash_profile
source /server/tinyv_data/lw/blackWhriteKList/script/*.sh

LOG_DIR="/home/tinyv/data/lw/blackWhriteKList/logs"



DATE_YYMMHH=$(date '+%Y-%m-%d')
DATE_YYMM=$(date '+%Y-%m')
DATE_DD=$(date '+%d')
NOW=$(date '+%Y-%m-%d_%H-%M-%S')
TODAY_LOGDIR=$LOG_DIR/spark_log/$DATE_YYMM/$DATE_DD
RUN_LOG="$LOG_DIR/${NOW}.log"

#查看spark任务跑的状态
function check_spark_last_status(){
        #cluster本地日志路径
	LOG_FILE_PATH="$TODAY_LOGDIR-$NOW-running.log"
	#得到APP_ID
	APP_NAME=$(egrep -o "application_[0-9]+_[0-9]+"  $LOG_FILE_PATH |tail -1)
	#得到APP_STATIS
	STATUS=$(yarnstatus $APP_NAME)

	#如果失败
        if [ $STATUS == "FAILED" ];then
		#日志回收
		cd /home/tinyv/data/lw/blackWhriteKList/logs/spark_log/app_log
		yarnlogs $APP_NAME
		sleep 0.1
		if [ -f ./${APP_NAME}.log ];then
			#日志放到日志路径
			mv  ./${APP_NAME}.log  $TODAY_LOGDIR/
		else 
			echo "${APP_NAME}.log 日志不存在 请检查" >> $RUN_LOG
		fi

		echo "$(date)   $LOG_FILE_PATH 任务执行失败,程序退出" >> $RUN_LOG
		#发送报警邮件
		mail "$NOW   $LOG_FILE_PATH 任务执行失败,程序退出"

                exit 1

        elif [ $STATUS == 'SUCCEEDED' ];then
			echo "${APP_NAME} 任务执行成功" >> $RUN_LOG
			#日志回收
			 cd /home/tinyv/data/lw/blackWhriteKList/logs/spark_log/app_log
			 yarnlogs $APP_NAME
			 sleep 0.1
			 if [ -f ./${APP_NAME}.log ];then
			          mv  ./${APP_NAME}.log  $TODAY_LOGDIR/
			else
			        echo "${APP_NAME}.log 日志不存在 请检查" >> $RUN_LOG
			fi
		else
			echo "未知的返回  $STATUS" >> $RUN_LOG
			exit 1
        fi
}
