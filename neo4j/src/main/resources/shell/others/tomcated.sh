#!/bin/bash

#\033[31m 红色字 \033[0m
function tomcat_status(){

	pid_file_num=$(ps -ef |grep "tomcat"|grep "7400"|grep "start" |grep  -v "vim" |wc -l)
	if [ $pid_file_num -eq 0 ];then
		echo -e "tomcat 7400 \033[31mnot alived\033[0m!"
		return 1
	else
		echo -e "tomcat 7400 \033[32malived\033[0m"
		return 0
	fi


}
function start_tomcat_7400(){

	tomcat_status
	if [ $? -eq 0 ];then
		echo -e "start failed,tomcat 7400 \033[31maready exist\033[0m"
	else
		cd /server/tomcat-7400
		echo -e "will be start tomcat 7400"
		nohup /server/tomcat-7400/bin/startup.sh &>/dev/null &
		sleep 5
		tomcat_status
		if [ $? -eq 0 ];then
			echo -e "start tomcat 7400 \033[32msuccess\033[0m"
			return 0
		else
			echo -e "start tomcat 7400 failed"
			return 1
		fi
	fi


}


function stop_tomcat_7400(){
	tomcat_status
	if [ $? -eq 0 ];then
		echo "will be stoped tomcat 7400"
		ps -ef |grep "tomcat"|grep "7400"|grep "start"| grep -v "vim"|awk '{print $2}'|xargs kill -9
		tomcat_status
		if [ $? -eq 0 ];then
			echo -e "stop \033[32msuccess\033[0m"
			return 0
		else
			echo -e "stop \033[31mfailed\033[0m"
			return 1
		fi
	else
		echo -e "stop failed,tomcat 7400 \033[31mnot exist\033[0m"
		return 1
	fi
}


function deploy_tomcat(){
	if [ -f /server/jars/7400/neo4j.war ];then
		stop_tomcat_7400
		rm -rf /server/tomcat-7400/webapps/*
		cp /server/jars/7400/neo4j.war /server/tomcat-7400/webapps/
		start_tomcat_7400
		echo -e "deploy new lable \033[32msuccess\033[0m"
	else
		echo -e "war file \033[31mnot exist\033[0m"
	fi

}


case $1 in
"start")
	start_tomcat_7400
;;
"stop")
	stop_tomcat_7400
;;
"restart")
	stop_tomcat_7400
	start_tomcat_7400
;;
"deploy")
	deploy_tomcat
;;
"status")
	tomcat_status
;;
*)
	echo -e "args[0] input error"
;;
esac
