old

OLDIFS=$IFS;IFS=";";arr=();arr+=($($BB printf '%s;' PATH_FOR_REPLACE/* | $BB grep "GREP_STRING_FOR_REPLACE"));IFS=$OLDIFS;FOLDER_SORT_PARAM=FOLDER_SORT_PARAM_FOR_REPLACE;FILE_SORT_PARAM=FILE_SORT_PARAM_FOR_REPLACE;DIR_SIZE_SHOW=DIR_SIZE_SHOW_FOR_REPLACE;FILE_SIZE_SHOW=FILE_SIZE_SHOW_FOR_REPLACE;DATE_SHOW=DATE_SHOW_FOR_REPLACE;DATE_SORT=DATE_SORT_FOR_REPLACE;SORT_DATE_NEW_TO_OLD_PARAM=SORT_DATE_NEW_TO_OLD_PARAM_FOR_REPLACE;for obj in "${arr[@]}";do detected="false";if [ "$DATE_SHOW" == "1" ];then d=$($BB date -r "${obj}"); else d="";fi;s="0.0B";if [ "$DATE_SORT" == "1" ]; then FOLDER_SORT_PARAM=$d;FILE_SORT_PARAM=$d;fi; if [ -L "$obj" ];then if [ -d "$obj" ];then echo "$FOLDER_SORT_PARAM;$obj;ld;$d;$s";detected="true";fi;if [ -f "$obj" ];then echo "$FILE_SORT_PARAM;$obj;lf;$d;$s";detected="true";fi; else if [ -d "$obj" ];then if [ "$DIR_SIZE_SHOW" == "1" ];then s=$($BB du -sh "$obj" | $BB awk '{print $1}');else s="0.0B";fi; echo "$FOLDER_SORT_PARAM;$obj;d;$d;$s";detected="true";fi;if [ -f "$obj" ];then if [ "$FILE_SIZE_SHOW" == "1" ];then s=$($BB du -sh "$obj" | $BB awk '{print $1}');else s="0.0B";fi;echo "$FILE_SORT_PARAM;$obj;f;$d;$s";detected="true";fi;fi;if [[ "$detected" == "false" && -e "$obj" ]];then echo "$FILE_SORT_PARAM;$obj;f;$d;$s";fi;done | $BB sort $SORT_DATE_NEW_TO_OLD_PARAM


OLDIFS=$IFS
IFS=";"
arr=()
arr+=($($BB printf '%s;\n' PATH_FOR_REPLACE/* | $BB grep "GREP_STRING_FOR_REPLACE"))
IFS=$OLDIFS
FOLDER_SORT_PARAM=FOLDER_SORT_PARAM_FOR_REPLACE
FILE_SORT_PARAM=FILE_SORT_PARAM_FOR_REPLACE
DIR_SIZE_SHOW=DIR_SIZE_SHOW_FOR_REPLACE
FILE_SIZE_SHOW=FILE_SIZE_SHOW_FOR_REPLACE
DATE_SHOW=DATE_SHOW_FOR_REPLACE
DATE_SORT=DATE_SORT_FOR_REPLACE
SORT_DATE_NEW_TO_OLD_PARAM=SORT_DATE_NEW_TO_OLD_PARAM_FOR_REPLACE
for obj in "${arr[@]}"
 do 
 detected="false"
 if [ "$DATE_SHOW" == "1" ]
	then
 	d=$($BB date +%Y-%m-%d' '%H:%M:%S -r "${obj}")
 else
	d=""
 fi
 s="0.0B"
 if [ "$DATE_SORT" == "1" ]
	then 
	FOLDER_SORT_PARAM=$d
	FILE_SORT_PARAM=$d
 fi
 if [ -L "$obj" ] 
	then 
	if [ -d "$obj" ]
		then 
		echo "$FOLDER_SORT_PARAM;$obj;ld;$d;$s"
		detected="true"
	fi
	if [ -f "$obj" ]
		then 
		echo "$FILE_SORT_PARAM;$obj;lf;$d;$s"
		detected="true"
	fi
 else 
	if [ -d "$obj" ]
		then 
		if [ "$DIR_SIZE_SHOW" == "1" ]
			then
			s=$($BB du -sh "$obj" | $BB awk '{print $1}')
		else
			s="0.0B"
		fi
		echo "$FOLDER_SORT_PARAM;$obj;d;$d;$s"
		detected="true"
	fi
	if [ -f "$obj" ]
		then
		if [ "$FILE_SIZE_SHOW" == "1" ]
			then
			s=$($BB du -sh "$obj" | $BB awk '{print $1}')
		else
			s="0.0B"
		fi
		echo "$FILE_SORT_PARAM;$obj;f;$d;$s"
		detected="true"
	fi
 fi 
 if [[ "$detected" == "false" && -e "$obj" ]]
	then 
	echo "$FILE_SORT_PARAM;$obj;f;$d;$s"
 fi
done | $BB sort $SORT_DATE_NEW_TO_OLD_PARAM




new

private static final String GET_ALL_DIR_OBJECTS_AND_SORT = "BB=BUSYBOX_FOR_REPLACE;OLDIFS=$IFS;IFS=\";\n\";arr=();arr+=($($BB printf '%s;\n' \"PATH_FOR_REPLACE\"/* | $BB grep \"GREP_STRING_FOR_REPLACE\"));IFS=$OLDIFS;FOLDER_SORT_PARAM=FOLDER_SORT_PARAM_FOR_REPLACE;FILE_SORT_PARAM=FILE_SORT_PARAM_FOR_REPLACE;DIR_SIZE_SHOW=DIR_SIZE_SHOW_FOR_REPLACE;FILE_SIZE_SHOW=FILE_SIZE_SHOW_FOR_REPLACE;DATE_SHOW=DATE_SHOW_FOR_REPLACE;DATE_SORT=DATE_SORT_FOR_REPLACE;SORT_DATE_NEW_TO_OLD_PARAM=SORT_DATE_NEW_TO_OLD_PARAM_FOR_REPLACE;for obj in \"${arr[@]}\";do detected=\"false\";if [ \"$DATE_SHOW\" == \"1\" ];then d=$($BB date +%Y-%m-%d' '%H:%M:%S -r \"${obj}\"); else d=\"date off\";fi;s=\"0.0B\";if [ \"$DATE_SORT\" == \"1\" ]; then FOLDER_SORT_PARAM=$d;FILE_SORT_PARAM=$d;fi; if [ -L \"$obj\" ];then if [ -d \"$obj\" ];then echo \"$FOLDER_SORT_PARAM;$obj;ld;$d;$s\";detected=\"true\";fi;if [ -f \"$obj\" ];then echo \"$FILE_SORT_PARAM;$obj;lf;$d;$s\";detected=\"true\";fi; else if [ -d \"$obj\" ];then if [ \"$DIR_SIZE_SHOW\" == \"1\" ];then s=$($BB echo $($BB du -sh \"$obj\") | $BB cut -d ' ' -f1);else s=\"0.0B\";fi; echo \"$FOLDER_SORT_PARAM;$obj;d;$d;$s\";detected=\"true\";fi;if [ -f \"$obj\" ];then if [ \"$FILE_SIZE_SHOW\" == \"1\" ];then s=$($BB echo $($BB du -sh \"$obj\") | $BB cut -d ' ' -f1);else s=\"0.0B\";fi;echo \"$FILE_SORT_PARAM;$obj;f;$d;$s\";detected=\"true\";fi;fi;if [[ \"$detected\" == \"false\" && -e \"$obj\" ]];then echo \"$FILE_SORT_PARAM;$obj;f;$d;$s\";fi;done | $BB sort $SORT_DATE_NEW_TO_OLD_PARAM";

BB=BUSYBOX_FOR_REPLACE;
OLDIFS=$IFS;
IFS=";";
arr=();
arr+=($($BB printf '%s;' "PATH_FOR_REPLACE"/* | $BB grep "GREP_STRING_FOR_REPLACE"));
IFS=$OLDIFS;
FOLDER_SORT_PARAM=FOLDER_SORT_PARAM_FOR_REPLACE;
FILE_SORT_PARAM=FILE_SORT_PARAM_FOR_REPLACE;
DIR_SIZE_SHOW=DIR_SIZE_SHOW_FOR_REPLACE;
FILE_SIZE_SHOW=FILE_SIZE_SHOW_FOR_REPLACE;
DATE_SHOW=DATE_SHOW_FOR_REPLACE;
DATE_SORT=DATE_SORT_FOR_REPLACE;
SORT_DATE_NEW_TO_OLD_PARAM=SORT_DATE_NEW_TO_OLD_PARAM_FOR_REPLACE;
for obj in "${arr[@]}"; do 
	detected="false";
	if [ "$DATE_SHOW" == "1" ];then 
		d=$($BB date +%Y-%m-%d' '%H:%M:%S -r "${obj}"); 
	else 
		d="date off";
	fi;
	s="0.0B";
	if [ "$DATE_SORT" == "1" ]; then 
		FOLDER_SORT_PARAM=$d;
		FILE_SORT_PARAM=$d;
	fi; 
	if [ -L "$obj" ];then 
		if [ -d "$obj" ];then 
			echo "$FOLDER_SORT_PARAM;$obj;ld;$d;$s";
			detected="true";
		fi;
		if [ -f "$obj" ];then 
			echo "$FILE_SORT_PARAM;$obj;lf;$d;$s";
			detected="true";
		fi; 
	else 	
		if [ -d "$obj" ];then 
			if [ "$DIR_SIZE_SHOW" == "1" ];then 
				s=$($BB echo $($BB du -sh "$obj") | $BB cut -d ' ' -f1);
			else 
				s="0.0B";
			fi; 
			echo "$FOLDER_SORT_PARAM;$obj;d;$d;$s";
			detected="true";
		fi;
		if [ -f "$obj" ];then 
			if [ "$FILE_SIZE_SHOW" == "1" ];then 
				s=$($BB echo $($BB du -sh "$obj") | $BB cut -d ' ' -f1);
			else 
				s="0.0B";
			fi;
			echo "$FILE_SORT_PARAM;$obj;f;$d;$s";detected="true";
		fi;
	fi;
	if [[ "$detected" == "false" && -e "$obj" ]];then 
		echo "$FILE_SORT_PARAM;$obj;f;$d;$s";
	fi;
done | $BB sort $SORT_DATE_NEW_TO_OLD_PARAM




new2

BB=/data/data/ru.scorpio92.filemanager/busybox; 
OLDIFS=$IFS;
IFS=";";
arr=();
arr+=($($BB printf '%s;' /* | $BB grep ""));
IFS=$OLDIFS;
for obj in "${arr[@]}"; do 
	detected="false";
	if [ -L "$obj" ];then 
		if [ -d "$obj" ];then 
			$BB stat "$obj";echo "Type: ld";
			detected="true";
		fi;
		if [ -f "$obj" ];then 
			$BB stat "$obj";echo "Type: lf";
			detected="true";
		fi; 
	else 	
		if [ -d "$obj" ];then 
			$BB stat "$obj";echo "Type: d";
			detected="true";
		fi;
		if [ -f "$obj" ];then 
			$BB stat "$obj";echo "Type: f";
			detected="true";
		fi;
	fi;
	if [[ "$detected" == "false" && -e "$obj" ]];then 
		$BB stat "$obj";echo "Type: f";
	fi;
done;


BB=/data/data/ru.scorpio92.filemanager/busybox; OLDIFS=$IFS; IFS=";"; arr=(); arr+=($($BB printf '%s;' /* | $BB grep "")); IFS=$OLDIFS; for obj in "${arr[@]}"; do detected="false"; if [ -L "$obj" ];then if [ -d "$obj" ];then $BB stat "$obj";echo "Type: ld"; detected="true"; fi; if [ -f "$obj" ];then $BB stat "$obj";echo "Type: lf"; detected="true"; fi; else if [ -d "$obj" ];then $BB stat "$obj";echo "Type: d"; detected="true"; fi; if [ -f "$obj" ];then  $BB stat "$obj";echo "Type: f"; detected="true"; fi; fi; if [[ "$detected" == "false" ]];then $BB stat "$obj";echo "f"; fi; done;

BB=/data/data/ru.scorpio92.filemanager/busybox; OLDIFS=$IFS; IFS=";"; arr=(); arr+=($($BB printf '%s;' /* | $BB grep "")); IFS=$OLDIFS; for obj in "${arr[@]}"; do detected="false"; if [ -L "$obj" ];then if [ -d "$obj" ];then echo "$obj;ld"; detected="true"; fi; if [ -f "$obj" ];then echo "$obj;lf"; detected="true"; fi; else if [ -d "$obj" ];then echo "$obj;d"; detected="true"; fi; if [ -f "$obj" ];then echo "$obj;f"; detected="true"; fi; fi; if [[ "$detected" == "false" ]];then echo "$obj;f"; fi; done;

BB=/data/data/ru.scorpio92.filemanager/busybox; OLDIFS=$IFS; IFS=";"; arr=(); arr+=($($BB printf '%s;' /* | $BB grep "")); IFS=$OLDIFS; for obj in "${arr[@]}"; do d=$($BB date +%Y-%m-%d' '%H:%M:%S -r "${obj}"); detected="false"; if [ -L "$obj" ];then if [ -d "$obj" ];then echo "$d;$obj;ld"; detected="true"; fi; if [ -f "$obj" ];then echo "$d;$obj;lf"; detected="true"; fi; else if [ -d "$obj" ];then echo "$d;$obj;d"; detected="true"; fi; if [ -f "$obj" ];then echo "$d;$obj;f"; detected="true"; fi; fi; if [[ "$detected" == "false" ]];then echo "$d;$obj;f"; fi; done;



