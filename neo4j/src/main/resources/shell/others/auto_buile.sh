#!/bin/bash
source /etc/profile
PORT_FILE="/server/port_file"
CATALINA_BASE="/server/tomcat-"
Keyword="开始请求"
NOW_DAY=$(date  '+%Y-%m-%d')
num=0

function check_war_file_exist_or_else(){
	#deploy war file
	if [ ! -f /server/data/readeQueue.war ];then
			echo -e "\nfile /server/data/readeQueue.war not exist\n"
			exit 1
	fi

	#deploy Example file
	if [ ! -f /server/port_file ];then
			echo -e "\nfile /server/port_file not exist\n"
			exit 1
	fi

	#deploy info result file
	if [ ! -d /server/start_info ];then
			echo -e "\nfile /server/start_info not exist, will be touch it.\n"
			mkdir /server/start_info
	fi
}

function check_tomcat_start_or_else(){
        PORT=$1
        CATALINA_BASE=/server/tomcat-$PORT
        NOW_DAY=$(date  '+%Y-%m-%d')
	NOW_MIN=$(date +'%H:%M')
        PID=$(ps -ef|grep java|grep "$CATALINA_BASE"|awk '{print $2}')
        PID_EXSIT_OR_ELSE=$(ps -ef|grep java|grep "$CATALINA_BASE"|grep -v 'grep'|awk '{print $2}'|wc -l)

	#log real refresh or else
        LOG_REAL_REFRESH=$(tail -20 $CATALINA_BASE/logs/catalina.out|grep "$NOW_DAY"| grep "$NOW_MIN"|grep -v 'grep'|wc -l)
        if [ $PID_EXSIT_OR_ELSE -gt 0 ];then
			if [ $LOG_REAL_REFRESH -gt 0 ];then
				#pid exist and log refreshed
				echo "pid exist and log refreshed"
				return 0
			else
				#pid exist and log not refreshed
				echo "pid exist and log  not refreshed"
				return 1
			fi
		else
			#pid not exist
			echo "pid not exist"
			return 2
		fi
}

function run_tomcat(){

        PORT=$1
	NOW_DAY=$(date  '+%Y-%m-%d')
	NOW_MIN=$(date +'%H:%M')
        CATALINA_BASE=/server/tomcat-$PORT

	   	echo "stop server $PORT,check pid and log"
		check_tomcat_start_or_else $PORT
		if [[ $? -eq 0 || $? -eq 1 ]];then

			#delete old wars
			rm -rf $CATALINA_BASE/webapps/readeQueue*

			#deploy new wars
			cp  /server/data/readeQueue.war  $CATALINA_BASE/webapps

			#stop this tomcat
			cd $CATALINA_BASE/bin
			ps -ef|grep java|grep "$CATALINA_BASE"|grep -v 'grep'|awk '{print $2}'|xargs kill -9 &>/dev/null
			sleep 1
			PID_NUM=$(ps -ef|grep java|grep "$CATALINA_BASE"|grep -v 'grep'|awk '{print $2}'|wc -l)
			if [ $PID_NUM -eq 0 ];then
				echo "shutdown $PORT Success"
			else
				echo "shutdown $PORT failed"
				return 1

			fi

			:> $CATALINA_BASE/logs/catalina.out

			#echo in logs 200 empty lines , because this time check the logs maybe carry last time record.
			for i in $(seq 200);do
				echo  -e "\n" >> $CATALINA_BASE/logs/catalina.out
			done

			#start the tomcat
			nohup ./startup.sh &>/dev/null &
			sleep 2
			echo -e "\nstart job down ,check it in effect or else"
			check_tomcat_start_or_else $PORT

			#judge keyword get in tomcat logs
                        for i in $(seq 61);do
                                echo "$i s"
                                SE=$(tail -200 $CATALINA_BASE/logs/catalina.out | grep "$Keyword" |wc -l)
				if [ $SE -gt 0 ];then
					echo "get Keyword($Keyword)! ----- num == $SE"
                                        break
                                fi
                                sleep 1

				#How long will the execution process take
				((num++))

				#if more then 60s, Think execution failure,Exit the loop and execute the next job
				if [ $i -eq 61 ];then
					echo "$PORT | \033[31mFAILED\033[0m  | 60s |$(date)" >> /server/start_info/$NOW_DAY
                                fi
                        done

			#Record the time spent executing successfully
			echo -e "$PORT | \033[32mSUCCESS\033[0m | ${num}s |$(date)" >> /server/start_info/$NOW_DAY
			num=0

		elif [ $? -eq 1 ];then

                        #echo in logs 200 empty lines , because this time check the logs maybe carry last time record.
                        for i in $(seq 200);do
                        	echo  -e "\n" >> $CATALINA_BASE/logs/catalina.out
                        done


			nohup ./startup.sh &>/dev/null &
			sleep 2
			echo -e "\nstart job down ,check it in effect or else"
			check_tomcat_start_or_else $PORT
			#judge keyword get in tomcat logs
			for i in $(seq 61);do
				echo "$i s"
				SE=$(tail -200 $CATALINA_BASE/logs/catalina.out | grep "$Keyword"|wc -l)
				if [ $SE -gt 0 ];then
					echo "get Keyword($Keyword)! ----- num == $SE"
					break
				fi

				#How long will the execution process take
				((num++))

				#if more then 60s, Think execution failure,Exit the loop and execute the next job
				sleep 1
				if [ $i -eq 61 ];then
echo $PORT
					echo "$PORT | \033[31mFAILED\033[0m  | 60s |$(date)" >> /server/start_info/$NOW_DAY
				fi
			done

			#Record the time spent executing successfully
			echo -e "$PORT | \033[32mSUCCESS\033[0m | ${num}s |$(date)" >> /server/start_info/$NOW_DAY
			num=0
       	fi
}



echo -e "------------------------------------------------------------------" >>/server/start_info/$NOW_DAY
echo -e "\n\n------------------$(date)--------------------" >>/server/start_info/$NOW_DAY
for i in $(cat $PORT_FILE|grep -v "^#");do
	run_tomcat $i
done
