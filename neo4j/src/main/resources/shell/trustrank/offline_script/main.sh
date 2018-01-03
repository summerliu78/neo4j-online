#!/bin/bash

source /etc/profile
source ~/.bash_profile

#本地数据路径
DATA_LOCAL_DIR='/server/TRdata/RankFileUpdate/data'
#脚本路径
SCRIPT_DIR='/server/TRdata/RankFileUpdate/script'
#时间格式
DATE_LOG_FORMAT=$(date '+%Y-%m-%d %H:%M:%S')
DATE_YYMMHH=$(date '+%Y-%m-%d')
DATE_YESTERDAY_YYMMHH=$(date -d 'yesterday' '+%Y-%m-%d')
#日志绝对路径
LOG_INFO="/server/TRdata/RankFileUpdate/log/${DATE_YYMMHH}.log"

#TR输入数据位置
RANK_NEED_FILE_DIR="/server/TRdata/RankFileUpdate/data/rankNeed"
#TR输出数据位置
#RANK_RESULT_FILE_DIR="/server/TRdata/RankFileUpdate/data/rankResult"
#TR输入数据备份文件位置
RANK_BACKUP_FILE_DIR="/server/TRdata/RankFileUpdate/data/rankNeed/backup"

TXT_FILE_NAME="/server/TRdata/RankFileUpdate/data"


#数据类型
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



#本地MD5
LOCAL_MD5=$(md5sum $DATA_LOCAL_DIR/data-${DATE_YYMMHH}.tgz |awk '{print $1}')
#远端传过来的MD5
REMOTE_MD5=$(cat $DATA_LOCAL_DIR/md5.txt)

#删除旧的解压后目录和文件
cd $DATA_LOCAL_DIR

log "INFO" "Delete History Data" "Get into Directory $DATA_LOCAL_DIR." "$LOG_INFO"
rm -rf ./{blacklist,relationship,whitelist,onedegree,blacklist.txt,relationship.txt,whitelist.txt,onedegree.txt}
log "INFO" "Delete History Data" "Delete last times data success." "$LOG_INFO"


#判断传输过来的数据是否完整
if [ $LOCAL_MD5 != $REMOTE_MD5 ];then
	log "ERROR" "Diff Md5" "The data on both sides are inconsistent. Local md5 is <$LOCAL_MD5> and remote md5 is <$REMOTE_MD5>." "$LOG_INFO"
	exit 1
else
	log "INFO" "Diff Md5" "The contrast is successful, and the data on both sides are consistent md5 is <$LOCAL_MD5>." "$LOG_INFO"
	
fi
#解压数据
tar -zxvf $DATA_LOCAL_DIR/data-${DATE_YYMMHH}.tgz -C $RANK_NEED_FILE_DIR

if [ $? -ne 0 ];then
	log "ERROR" "Decompression" "Decompression file data-${DATE_YYMMHH}.tgz faild" "$LOG_INFO"
else
	log "INFO" "Decompression" "Decompression file data-${DATE_YYMMHH}.tgz success" "$LOG_INFO"
fi
for i in  $TYPE_USE_TO_LOOP;do
	log "INFO" "Handle Data" "Start Handle Data $i" "$LOG_INFO"
	sh -x $SCRIPT_DIR/handle.sh "$i"
	log "INFO" "Handle Data" "End Handle Data $i" "$LOG_INFO"

done

#数据整合备份
for i in $TYPE_USE_TO_LOOP;do
#	sh -x $SCRIPT_DIR/handle.sh "$i"
	#本次更新产生文件
	NEW_FILE_NAME="$RANK_NEED_FILE_DIR/${i}.txt.1"
	#上次更新产生文件
	OLD_FILE_NAME="$RANK_NEED_FILE_DIR/${i}.txt"
	#本次更新文件内容计数
	num_new_file_lines=$(wc -l $NEW_FILE_NAME | awk '{print $1}')
	#上次更新文件内容计数
	num_old_file_lines=$(wc -l $OLD_FILE_NAME | awk '{print $1}')
	#备份文件名称
	BACKUP_FILE_NAME="$RANK_BACKUP_FILE_DIR/$i-${DATE_YYMMHH}.txt"
	#首先判断，新文件存在并且有内容
	if [[ ! -f $NEW_FILE_NAME && $num_new_file_lines -ge 10000 ]];then
		log "ERROR" "$i File Errot" "Input file <$NEW_FILE_NAME> not exist or size error" "$LOG_INFO"
		exit 1
	fi
	
	#如果没有上次文件，即：第一次更新，或文件被误操作
	if [ ! -f $OLD_FILE_NAME ];then
		log "INFO" "Move File" "first,Move Data to used path" "$LOG_INFO"
		#把新文件移动过去
		\mv $NEW_FILE_NAME $OLD_FILE_NAME 
	#文件存在，增量更新情况
	else
		#新文件的条数一定是在增长的，如果小于的话这个就不更新了
		#@WARN [这块看看怎么做一下详细的diff]
		if [[ $num_new_file_lines -ge $num_old_file_lines ]];then
			log "INFO" "Move File" "Move old file to backup path" "$LOG_INFO"
			#先备份旧文件
			\mv $OLD_FILE_NAME $BACKUP_FILE_NAME
			log "INFO" "Move File" "Move new file to used path" "$LOG_INFO"
			#把新文件移动过去
			\mv $NEW_FILE_NAME $OLD_FILE_NAME
		#新文件比之旧文件数据要少 直接跳过这次循环
		else
			log "ERROR" "Move File" "new file <$i> data number less then old file data" "$LOG_INFO"
			exit 1
		fi
	fi
done





$SCRIPT_DIR/rank.sh
sleep 3
$SCRIPT_DIR/cleanData.sh
