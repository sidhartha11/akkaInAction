FILES="chapterUpAndRunning \
messageModule \
chapter-remoting \
chapter-conf-deploy \
chapter-futures \
otherModule \
scratchModule \
serverModule \
chapter6Remoting \
utilityModule" 
for file in $FILES
do
echo ":$file:"
cd ./$file
pwd
rm -rf ./.classpath ./.project ./.settings ./target
cd ..
done
