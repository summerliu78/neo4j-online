#!/bin/env bash
source /etc/profile
source ~/.bash_profile


ROOT_DIR='/home/tinyv/data/lw/blackWhriteKList'
LOG_DIR="$ROOT_DIR/logs"
FILE_DIR="$ROOT_DIR/file"

DATE_YYMMHH=$(date '+%Y-%m-%d')
DATE_YYMM=$(date '+%Y-%m')
DATE_DD=$(date '+%d')
NOW=$(date '+%Y-%m-%d_%H-%M-%S')
HDFS_PATH='/user/tinyv/neo4j/credit' 
HDFS_DATA_LOCAL_PATH="$ROOT_DIR/data"
TODAY_HDFS_DATA_LOCAL_PATH="$HDFS_DATA_LOCAL_PATH/$DATE_YYMMHH"

TODAY_LOGDIR=$LOG_DIR/spark_log/$DATE_YYMMHH
RUN_LOG="$LOG_DIR/${NOW}.log"


function dir_del_or_create(){
	TYPE=$1
	DIR=$2
	if [ $TYPE == 'delete' ];then
                if [  -d $DIR ];then
                    rm -rf $DIR
                fi
	elif [ $TYPE == 'create'  ];then
		if [ ! -d $DIR ];then
	        	mkdir -p $DIR
		fi
	fi
}


#shell 发送邮件的函数
function mail(){
        #发件人 昵称<邮箱>
		FROM="deploy_alarm<auto_deploy@yinker.com>"
		#收件人
		TO="liuwei1@yinker.com"
		#抄送
		CC=
		#邮件主题
		SUBJECT="团伙部署报警"
		#附件文件的绝对路径
	    #UUENCODE="$2"
        #正文
        INFO="$1"
        #uuencode 附件的绝对路径 附件名
        #uuencode $UUENaCODE "$log_name")
		(echo -e "To: $TO\nCC: $CC\nFrom: $FROM\nSubject: $SUBJECT\n\n$INFO") | sendmail -t
		#(echo -e "To: $TO\nCC: $CC\nFrom: $FROM\nSubject: $SUBJECT\n\n$INFO";uuencode $UUENaCODE "$log_name") | sendmail -t
}
#查看spark任务跑的状态
function check_spark_last_status(){
        #cluster本地日志路径
	LOG_FILE_PATH="$TODAY_LOGDIR/$NOW-running.log"
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
				  return 0
			else
			        echo "${APP_NAME}.log 日志不存在 请检查" >> $RUN_LOG
			fi
		else
			echo "未知的返回  $STATUS" >> $RUN_LOG
			exit 1
        fi
}


function check_get_data_from_hdfs_secsses_or_else(){

	LAST_RETURN_STATUS=$(echo "$?")
	HDFS_DIR=$1
	LOCAL_DIR=$2
	TYPE=$3
	HDFS_DATA_SIZE=$(hdfs dfs -du $LOCAL_DIR 2>/dev/null | grep "$TYPE"|awk '{print $1}')
	LOCAL_DATA_SIZE=$(du -b ${LOCAL_DIR}|grep "$TYPE" awk '{print $1}')
	DIFF_SIZE=$(echo "$HDFS_DATA_SIZE $LOCAL_DATA_SIZE"|awk '{printf  ("%.2f\n", sqrt(($1-$2)*($1-$2))/$1)}')
	if [ $DIFF_SIZE=="0.00" ];then
		echo "secsses! get $TYPE"
		return 0
	else
		echo "faild get $TYPE"
		return 1
	fi

}



function run_spark_job(){

JAR_NAME=/home/tinyv/hjj/neo4j/neo4j-service.jar
CLASSNAME=com.yinker.data.neo4j.Neo4jCallDetailCredit

cd /server/tinyv_data/lw/blackWhriteKList/script



dir_del_or_create create "$TODAY_LOGDIR"


spark-submit \
--executor-cores 2 \
--num-executors 15 \
--driver-memory 8G \
--queue tinyv_queue \
--executor-memory 8G \
--master yarn \
--deploy-mode cluster \
--conf spark.yarn.archive=hdfs:///user/tinyv/spark-libs.jar \
--conf spark.default.parallelism=90 \
--conf spark.debug.maxToStringFields=100 \
--conf spark.shuffle.sort.bypassMergeThreshold=4000 \
--class $CLASSNAME $JAR_NAME savePath=/user/tinyv/neo4j/credit  > $TODAY_LOGDIR/$NOW-running.log 2>&1
}



#DATE_YYMMHH
function get_data_from_hdfs(){
	dir_del_or_create create "$TODAY_HDFS_DATA_LOCAL_PATH"
	KEY="0"
	for i in "applycreditNoBorrow" "blacklist" "borrowNotwb" "whitelist";do
		if [ $? -ne 0 ];then 
			KEY=$KEY"-$i"
		fi
		dir_del_or_create delete "$TODAY_HDFS_DATA_LOCAL_PATH/$i"
		hdfs dfs -get $HDFS_PATH/$DATE_YYMMHH/$i $TODAY_HDFS_DATA_LOCAL_PATH/
		check_get_data_from_hdfs_secsses_or_else "$TODAY_HDFS_DATA_LOCAL_PATH" "$TODAY_HDFS_DATA_LOCAL_PATH" $i
	done

	if [ $KEY != "0"   ];then
		echo "have some err in get data from hdfs"
		exit 1
	fi
	if [ -f $TODAY_HDFS_DATA_LOCAL_PATH/data-${DATE_YYMMHH}.tgz ];then
		rm -f $TODAY_HDFS_DATA_LOCAL_PATH/data-${DATE_YYMMHH}.tgz
	fi
	tar -zcvf $TODAY_HDFS_DATA_LOCAL_PATH/data-${DATE_YYMMHH}.tgz $TODAY_HDFS_DATA_LOCAL_PATH/{applycreditNoBorrow,blacklist,borrowNotwb,whitelist}

	md5=md5sum $TODAY_HDFS_DATA_LOCAL_PATH/data-${DATE_YYMMHH}.tgz|awk  '{print $1}'
	echo "$md5" > $FILE_DIR/md5.txt



}





#跑spark任务
run_spark_job
#检查跑的结果
check_spark_last_status
#down数据并且打包
get_data_from_hdfs

