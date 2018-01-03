#!/bin/env bash
source /etc/profile
source ~/.bash_profile





#本地基础目录
ROOT_DIR='/home/tinyv/data/lw/blackWhriteKList'
#日志目录
LOG_DIR="$ROOT_DIR/logs"
#相关其他文件目录 （MD5）
FILE_DIR="$ROOT_DIR/file"
#application日志目录
APPLICATION_DIR="$LOG_DIR/spark_log/app_log"

#年-月-日
DATE_YYMMHH=$(date '+%Y-%m-%d')
#年-月-日(昨天)
DATE_YESTERDAY_YYMMHH=$(date -d "yesterday" '+%Y-%m-%d')
DATE_LOG_FORMAT=$(date '+%Y-%m-%d %H:%M:%S')
#DATE_YESTERDAY_YYMMHH="2017-11-01"
#年-月
DATE_YYMM=$(date '+%Y-%m')
#日
DATE_DD=$(date '+%d')
#年-月-日_时-分-秒
NOW=$(date '+%Y-%m-%d_%H-%M-%S')
#hdfs数据生成目录
HDFS_PATH='/user/tinyv/neo4j/trustrank'
#落地到文件系统的目录
HDFS_DATA_LOCAL_PATH="$ROOT_DIR/data"
#落地目录子目录（当日）
TODAY_HDFS_DATA_LOCAL_PATH="$HDFS_DATA_LOCAL_PATH/$DATE_YYMMHH"
#当日spark日志目录
TODAY_LOGDIR=$LOG_DIR/spark_log/$DATE_YYMMHH
#脚本DEBUG日志目录详情
RUN_LOG="$LOG_DIR/app_log/${NOW}.log"

#远端文件目录（生成文件传到的目录）
REMOTE_DATA_FILE="/server/TRdata/RankFileUpdate/data"

#jar包目录
JAR_NAME='/home/tinyv/data/lw/blackWhriteKList/jar/neo4j-service.jar'
#spark任务全类名
CLASSNAME='com.yinker.data.neo4j.TrustRankData'
#spark任务SaveASTextFile路径
SPARK_JOB_SAVE_DIR='/user/tinyv/neo4j/trustrank'
#spark conf info
HBASE_CONFIG_PATH=/usr/hdp/current/hbase-client/conf/
SPARK_CLIENT_HOME=/usr/hdp/current/spark-client
LIB_HOME=$SPARK_CLIENT_HOME/lib
HIVE_SITE_FILE=$SPARK_CLIENT_HOME/conf/hive-site.xml
JARS="$LIB_HOME/datanucleus-api-jdo-3.2.6.jar,$LIB_HOME/datanucleus-core-3.2.10.jar,$LIB_HOME/datanucleus-rdbms-3.2.9.jar,
/usr/hdp/2.4.2.0-258/hive/auxlib/tinyv_json_serde_1.3.8.jar"


REMOTE_ADDRESS="10.2.20.118"
REMOTE_USERNAME="liuwei1"
#REMOTE_USERNAME="root"
REMOTE_DATA_DIR="/server/TRdata/RankFileUpdate/data"

TYPE_USE_TO_FILE="blacklist,relationship,whitelist,onedegree"
TYPE_USE_TO_LOOP="blacklist relationship whitelist onedegree"
TYPE_USE_TO_TAR="./blacklist ./relationship ./whitelist ./onedegree"


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

#目录处理函数
function dir_del_or_create(){
	TYPE=$1
	DIR=$2

	case $TYPE in
	delete)
		if [  -d $DIR ];then
			rm -rf $DIR
			log "INFO" "dir delete" "Directory $2 exist and will be delete" "$RUN_LOG"
			return 0
		else
			log "INFO" "dir delete" "Directory $2 not exist and do nothing" "$RUN_LOG"
			return 0
		fi
	;;
	create)
                if [ ! -d $DIR ];then
			log "INFO" "dir create" "Directory $2 not exist and will be create" "$RUN_LOG"
			mkdir -p $DIR
			return 0
		else
			log "INFO" "dir create" "Directory $2  exist and do nothing" "$RUN_LOG"
			return 0
                fi
	;;
	*)
		log "ERROR" "function args error" "function args not in [create/delete]" "$RUN_LOG"
		exit 1
	;;
	esac
}

#WILL 邮件这块看一看
#shell 发送邮件的函数
function mail(){
        #发件人 昵称<邮箱>
		FROM="deploy_alarm<auto_deploy@yinker.com>"
		#收件人
		TO="liuwei1@yinker.com"
		#抄送
		CC=
		#邮件主题
		SUBJECT="TrustRank"
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
#	LOG_FILE_PATH="$TODAY_LOGDIR/2017-12-11_10-33-33-running.log "
	#得到APP_ID
	APP_NAME=$(egrep -o "application_[0-9]+_[0-9]+"  $LOG_FILE_PATH |tail -1)
	#得到APP_STATIS
	STATUS=$(yarnstatus $APP_NAME)

	#如果失败
        if [ $STATUS == "FAILED" ];then
		#日志回收
		cd $APPLICATION_DIR
		#只能通过32的tinyv用户执行 yarn的相关命令只是本地写了
		#将app日志拉到app日志路径
		#yarnlogs $APP_NAME $TODAY_LOGDIR/
		sleep 0.1
#		if [ -f ./${APP_NAME}.log ];then
#			#日志放到日志路径
#			mv  ./${APP_NAME}.log  $TODAY_LOGDIR/
#		else
#			echo "${APP_NAME}.log 日志不存在 请检查" >> $RUN_LOG
#		fi

		log "ERROR" "Application status" "application status is FAILED,job will be exit" "$RUN_LOG"

		#发送报警邮件
	#	mail "$NOW   $LOG_FILE_PATH 任务执行失败,程序退出"
		#spark任务执行失败 程序退出
                exit 1

        elif [ $STATUS == 'SUCCEEDED' ];then
		log "INFO" "Application status" "application status is SUCCESS" "$RUN_LOG"
		#日志回收
		 cd $APPLICATION_DIR
		 #yarnlogs $APP_NAME $TODAY_LOGDIR/
		 sleep 0.1
#		 if [ -f ./${APP_NAME}.log ];then
#		          mv  ./${APP_NAME}.log  $TODAY_LOGDIR/
#			  return 0
#		else
#		        echo "${APP_NAME}.log 日志不存在 请检查" >> $RUN_LOG
#		fi
		return 0
	else
		log "ERROR" "Application status" "application status is other status,will be exit" "$RUN_LOG"
		exit 1
        fi
}

#检查拉取数据是否完整 参数： $1:HDFS路径 $2:本地路径 $3：拉取类型（边关系或黑白名单）
function check_get_data_from_hdfs_secsses_or_else(){
	#查看get数据命令的返回值
	LAST_RETURN_STATUS=$(echo "$?")
	#hdfs路径 数据的父目录
	HDFS_DIR=$1
	#get to 的地址
	LOCAL_DIR=$2
	TYPE=$3
	#hdfs相关数据大小 字节为单位
	HDFS_DATA_SIZE=$(hdfs dfs -du $HDFS_DIR 2>/dev/null | grep "$TYPE"|awk '{print $1}')
	#down到本地文件系统的数据大小 字节为单位
	LOCAL_DATA_SIZE=$(du -b $LOCAL_DIR | grep "$TYPE" | awk '{print $1}')
	#如果差值在1%以下 那就认为数据完整 hdfs和本地在计算大小上存在一定差别 具体是因为inode或者是什么原因未知
	DIFF_SIZE=$(echo "$HDFS_DATA_SIZE $LOCAL_DATA_SIZE"|awk '{printf  ("%.2f\n", sqrt(($1-$2)*($1-$2))/$1)}')
	#如果在百分之一以下认为成功
	if [ $DIFF_SIZE=="0.00" ];then
		log "INFO" "check get data" "Data diff less then 1%,Success" "$RUN_LOG"
		return 0
	#如果差别在百分位 认为读取失败了
	else
		log "INFO" "check get data" "Data diff more then 1%,will be exit" "$RUN_LOG"
		return 1
	fi
}



function run_spark_job(){

hdfs dfs -rm -r  "$HDFS_PATH/$DATE_YESTERDAY_YYMMHH" &>/dev/null
	dir_del_or_create create "$TODAY_LOGDIR"
	que=$1
#--queue tinyv_queue \
#--queue default \
spark-submit \
--executor-cores 2 \
--num-executors 30 \
--driver-memory 8G \
--queue $que \
--executor-memory 8G \
--master yarn \
--deploy-mode client \
--conf spark.yarn.archive=hdfs:///user/tinyv/spark-libs.jar \
--conf spark.default.parallelism=180 \
--files /opt/spark-2.1.0-bin-without-hadoop/conf/hive-site.xml \
--class "$CLASSNAME" "$JAR_NAME"  "savePath=$SPARK_JOB_SAVE_DIR"  > $TODAY_LOGDIR/$NOW-running.log 2>&1
}

#--conf spark.eventLog.enabled=ture \


#DATE_YYMMHH
#从hdfs取数据函数
function get_data_from_hdfs(){
	#创建数据当天的存储目录
	dir_del_or_create create "$TODAY_HDFS_DATA_LOCAL_PATH"
	# hdfs目录名字
	for i in $TYPE_USE_TO_LOOP;do
		retry_num=5
		while [ $retry_num -gt 0 ] ;do
			#先删除之前的执行结果get到的文件
			dir_del_or_create delete "$TODAY_HDFS_DATA_LOCAL_PATH/$i"
			#将数据down到本地
			hdfs dfs -get $HDFS_PATH/$DATE_YESTERDAY_YYMMHH/$i $TODAY_HDFS_DATA_LOCAL_PATH/
			#检查数据完整性
			check_get_data_from_hdfs_secsses_or_else "$HDFS_PATH/$DATE_YESTERDAY_YYMMHH" "$TODAY_HDFS_DATA_LOCAL_PATH" $i
			#检查数据完整性的标记
			check_result=$?
			#如果完整，继续get下个数据
			if [ $check_result -eq 0 ];then
				break
			else
				#如果检查函数返回值不为零，即数据不完整，则重试
				((num--))
				if [ $num -gt 0 ];then
					log "ERROR" "get file" "get $i file error,There are $num more opportunities,exhaustion" "$RUN_LOG"
				fi
				#get失败，返回继续下次重试
		        	continue
			fi
		done
	done
}

function sync_data(){
	#打包后的文件名
	TAR_FILE_PATH="data-${DATE_YYMMHH}.tgz"
	#+ tar -zcPvf data-2017-12-05.tgz './{blacklist,relationship,whitelist,onedegree}'
	#tar: ./{blacklist,relationship,whitelist,onedegree}: Cannot stat: No such file or directory
	#当天数据生成路径
	NEW_DATA_PATH=$TYPE_USE_TO_TAR
	#生成的MD5校验和的文件
	MD5_FILE_PATH="$FILE_DIR/md5.txt"
	#删除当天的数据（情况就是，一天之内跑失败了，要删掉之前打的包）
	cd $TODAY_HDFS_DATA_LOCAL_PATH
	if [ -f $TAR_FILE_PATH ];then
		rm -f $TAR_FILE_PATH
		log "INFO" "delete tar file" "delete today tar file $TAR_FILE_PATH" "$RUN_LOG"
	fi
	#+ tar -zcPvf data-2017-12-05.tgz './{blacklist,relationship,whitelist,onedegree}'
	#tar: ./{blacklist,relationship,whitelist,onedegree}: Cannot stat: No such file or directory
	# 生成数据的名字
	tar -zcPvf $TAR_FILE_PATH $NEW_DATA_PATH
	log "INFO" "create tar file" "create tar file $TAR_FILE_PATH" "$RUN_LOG"
	#打包成功判断
	if [ $? -ne 0 ];then
		log "INFO" "create tar file" "create tar file $TAR_FILE_PATH FAILED" "$RUN_LOG"
		exit 1
	fi
	#截取MD5值
	md5=$(md5sum $TAR_FILE_PATH|awk  '{print $1}')
	log "INFO" "tar file MD5" "tar file MD5 is $md5" "$RUN_LOG"
	echo "$md5" > $MD5_FILE_PATH
	if [ -f $MD5_FILE_PATH ];then
		log "INFO" "tar file MD5" "create MD5 file success" "$RUN_LOG"
	fi


	#先删掉远端文件
	ssh $REMOTE_USERNAME@$REMOTE_ADDRESS "rm -f $REMOTE_DATA_DIR/$TAR_FILE_PATH"
	if [ $? -eq 0 ];then
		log "INFO" "Delete remote file" "delete remote tar file and md5 file success" "$RUN_LOG"
	else
		log "ERROR" "Delete remote file" "delete remote tar file and md5 file FAILED" "$RUN_LOG"
	fi
	#远程拷贝到远端目录
	scp -r $MD5_FILE_PATH $TAR_FILE_PATH $REMOTE_USERNAME@$REMOTE_ADDRESS:$REMOTE_DATA_DIR/
	#确认返回值
	if [ $? -eq 0 ];then
		log "INFO" "scp data" "scp data success" "$RUN_LOG"
	else
		log "ERROR" "scp data" "scp data FAILED" "$RUN_LOG"
		exit 1
	fi
	ssh $REMOTE_USERNAME@$REMOTE_ADDRESS "sh -x /server/TRdata/RankFileUpdate/script/main.sh &>~/aaaa.log &"

}

function clean_data(){

DAYS_AGO_15_DATA_FORMATE=$(date -d '15 days ago' '+%Y-%m-%d')
#保存天数
SAVE_SDAYS=15
#hdfs数据目录
HDFS_PATH="/user/tinyv/neo4j/trustrank/$DAYS_AGO_15_DATA_FORMATE"
#hdfs down下来的数据目录
HDFS_LOCAL_PATH='/server/tinyv_data/lw/blackWhriteKList/data'
#日志文件数据
APP_LOG_PATH='/server/tinyv_data/lw/blackWhriteKList/logs/app_log'
SPARK_LOG_PATH='/server/tinyv_data/lw/blackWhriteKList/logs/spark_log'

hdfs dfs -ls $HDFS_PATH &>/dev/null
if [ $? -eq 0 ];then
	hdfs dfs -rm -r $HDFS_PATH &>/dev/null
fi

#清理hdfs本地文件
find $HDFS_LOCAL_PATH/ -type d -mtime +$SAVE_SDAYS |xargs rm -rf
#清理本地日志
find $APP_LOG_PATH/ -type f -mtime +$SAVE_SDAYS |xargs rm -rf
#清理spark日志
find $SPARK_LOG_PATH/ -type d -mtime +$SAVE_SDAYS | xargs rm -rf

}

#################################################
##################          #####################
##################   main   #####################
##################          #####################
#################################################

#run_spark_job "default"
run_spark_job "tinyv_queue"
#
check_spark_last_status
#
get_data_from_hdfs
#
sync_data
#每天清理数据任务
clean_data