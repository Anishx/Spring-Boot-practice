# config/docker/start_sftp.sh
OLDIFS=$IFS;
IFS=$'\n'

for user in $(awk -F ':' '{print $1}' /etc/sftp/users.conf);
do
  usermod -d /sftp -g users -s /sbin/nologin $user
done

IFS=$OLDIFS

cp /etc/ssh/sshd_config /etc/ssh/sshd_config.bak
sed -i -e 's/%h/\/home\/%u/' /etc/ssh/sshd_config
/usr/sbin/sshd -D -E /var/log/auth.log