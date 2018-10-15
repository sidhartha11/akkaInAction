FILES="chapterUpAndRunning \
messageModule \
otherModule \
scratchModule \
serverModule \
utilityModule" 
for file in $FILES
do
echo ":$file:"
cd ./$file
pwd
rm -rf ./.classpath ./.project ./.settings
cd ..
done
