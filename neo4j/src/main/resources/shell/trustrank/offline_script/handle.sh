#!/bin/bash
#applycreditNoBorrow,blacklist,borrowNotwb,whitelist

#传入数据类型
TYPE=$1

#日期格式
DATE_YYMMHH=$(date '+%Y-%m-%d')
DATE_LOG_FORMAT=$(date '+%Y-%m-%d %H:%M:%S')
#日志路径
LOG_INFO="/server/TRdata/RankFileUpdate/log/${DATE_YYMMHH}.log"
#输入数据目录
INPUT_FILE_DIR="/server/TRdata/RankFileUpdate/data/rankNeed/$TYPE"
#数据文件路径
FILE_NAME="/server/TRdata/RankFileUpdate/data/rankNeed/${TYPE}.txt.1"


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


#判断是输入type是否标准
if [[ $TYPE != 'relationship' && $TYPE != 'onedegree' && $TYPE != 'blacklist'  && $TYPE != 'whitelist' ]];then
	
	echo "参数不匹配"
	log "ERROR" "Handle" "$TYPE not exist,will  be exit" "$LOG_INFO"
	exit 1
else 
	log "INFO" "Handle" "Will be handle data $TYPE" "$LOG_INFO"
fi

#判断输入数据路径是否存在
if [ ! -d "$INPUT_FILE_DIR" ];then
	log "ERROR" "Handle" "Input path $TYPE not exist, will be exist." "$LOG_INFO"
	exit 1
else 
	log "INFO" "Handle" "Read Input file path $INPUT_FILE_DIR success." "$LOG_INFO"
fi



	
log "INFO" "Handle" "Will be delete old file $FILE_NAME ." "$LOG_INFO"
#删掉原来存在的文件，因为是追加操作
rm -f $FILE_NAME


#exit 0
echo "$FILE_NAME"
#把所有partition文件导入单个文件中
log "Info" "Handle" "will be handle data $INPUT_FILE_DIR" "$LOG_INFO"
for i in `ls $INPUT_FILE_DIR`;do
	cat $INPUT_FILE_DIR/$i >>  $FILE_NAME
done
