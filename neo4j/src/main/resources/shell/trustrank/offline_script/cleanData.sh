#!/bin/bash

SAVE_DAYS=15
RANK_RESULT_PATH='/server/TRdata/RankFileUpdate/data/rankResult/backup'
RANK_NEED_PATH='/server/TRdata/RankFileUpdate/data/rankNeed/backup'
TAR_FILE_PATH='/server/TRdata/RankFileUpdate/data'
LOG_FILE_PATH='/server/TRdata/RankFileUpdate/log'

#离线分数备份文件清理
find $RANK_RESULT_PATH/ -name "trustrankResult*" -type f -mtime +15 | xargs rm -f

sleep 0.5

#离线数据输入数据文件清理
find $RANK_NEED_PATH/ -type f -mtime +15 | xargs rm -f

sleep 0.5

#处理前数据的压缩文件清理
find $TAR_FILE_PATH/ -name "data*tgz" -type f -mtime +15 | xargs rm -f

sleep 0.5

#日志文件清理
find $LOG_FILE_PATH/ -name "*log" -type f -mtime +15 | xargs rm -f
