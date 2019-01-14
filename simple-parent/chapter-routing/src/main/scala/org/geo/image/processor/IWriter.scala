package org.geo.image.processor

import akka.actor.{ Actor }
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import akka.actor.ActorLogging

case class ImageRect(bi: BufferedImage, x: Int, y: Int)
case class Status(stat: String)
case class FileLocation(fileno: String, x: Int, y: Int, bi: Option[BufferedImage]=None)
case class Finished(filename: String)
class IWriter(
  nmbrImages: Int,
  width:      Int,
  height:     Int,
  rows:       Int,
  cols:       Int,
  typ:        Int,
  )
  extends Actor  with ActorLogging {
  
  val finalImg = new BufferedImage(width * cols, height * rows, typ)

  var id = self.path.name
  log.info("IWriter initialized: id= %s".format(id))
  
  def dir = "C:\\workarea\\fixed2\\"
  def filename = "scalaEx.jpg"
  def receive = {

    case msg: FileLocation =>
      
      val x:Int  = msg.x 
      val y:Int  = msg.y
//      if ( x == -1 ) {
//      println("writing file: %s".format(msg))
//      val fln = dir + filename
//      println("filename = " + fln)
//      ImageIO.write(finalImg, "jpeg", new File(fln));
//      sender() ! Status("finished")        
//      } else {
      log.debug("processing message: {}" , msg )
      finalImg.createGraphics().drawImage(msg.bi.get,x,y,null)
      sender() ! Status("ok")
 //     }
      // finalImg.createGraphics().drawImage(bi, x, y, null);
      
      
      
    case Finished(filename) =>
      println("Finished image processing %s".format(filename))
      ImageIO.write(finalImg, "jpeg", new File(dir + filename));
      sender() ! Status("finished")
  }

}

//	public static class IWriter {
//		private final int nmbrImages;
//		private final int width;
//		private final int height;
//		private final int rows;
//		private final int cols;
//		private final int type;
//		private final BufferedImage finalImg;
//
//		public IWriter(int nmbrImages, int rows, int cols, int width, int height, int type) {
//			this.nmbrImages = nmbrImages;
//			this.width = width;
//			this.height = height;
//			this.rows = rows;
//			this.cols = cols;
//			this.type = type;
//			finalImg = createImage(nmbrImages, rows, cols, width, height, type);
//		}
//
//		public void addRect(String fileno, int x, int y) {
//			/** read in the image **/
//			System.out.println("adding " + fileno + ",x=" + x + ",y=" + y);
//			BufferedImage bi = null;
//			try {
//				bi = ImageIO.read(new File(fileno));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			int type = bi.getType();
////		    for (int i = 0; i &lt; chunks; i++) {
////		        buffImages[i] = ImageIO.read(imgFiles[i]);
//			finalImg.createGraphics().drawImage(bi, x, y, null);
//		}
//
//		public void writeImage(String filename) {
//			System.out.println("Image concatenated.....");
//			try {
//				ImageIO.write(finalImg, "jpeg", new File(dir + filename));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		private BufferedImage createImage(int nmbrImages, int rows, int cols, int width, int height, int type) {
//			return new BufferedImage(width * cols, height * rows, type);
//
//		}
//
//		public void checkSizeAndWrite(int siz) {
//			if (siz == nmbrImages) {
//				dumpImage();
//			}
//		}
//
//		private void dumpImage() {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public String toString() {
//			return "IWriter [nmbrImages=" + nmbrImages + ", width=" + width + ", height=" + height + ", rows=" + rows
//					+ ", cols=" + cols + ", type=" + type + ", finalImg=" + finalImg + "]";
//		}
//	}