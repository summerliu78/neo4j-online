#!/bin/bash
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
