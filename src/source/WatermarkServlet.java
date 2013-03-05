package source;
/*Servlet g�rant un syst�me d'achat d'images 
  Code inspir� de l'article "Watermarking images in a Java Servlet"
  Disponible au http://www.codebeach.com/tutorials/watermarking-images-java-servlet.asp
  Code modifi� par Eric Boivin, dans le cadre du cours GTI525, �t� 2007
*/


import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.ImageIcon;
import java.awt.geom.Rectangle2D;

public class WatermarkServlet extends HttpServlet
{
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    {
        if (req.getParameter("carteCredit") != ""){
            req.setAttribute("paye","true");  
            doGet(req, res);  
        }    
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
    {
        /*L'appel de ce servlet se fait en donnant l'url de la miniature de l'image.
          Ici, on remplace le chemin du r�pertoire de thumbnails par le r�pertoire 
          contenant les originaux.*/
        String path = req.getPathTranslated().replaceAll(getServletConfig().getInitParameter("dossierVignettes"),getServletConfig().getInitParameter("dossierOriginaux"));
		try
        {File file = new File(path);
            if (!file.exists())
            {
                res.sendError(res.SC_NOT_FOUND);
                return;
            }

            ImageIcon photo = new ImageIcon(path);

            BufferedImage bufferedImage = new BufferedImage(photo.getIconWidth(),
                    photo.getIconHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();

            g2d.drawImage(photo.getImage(), 0, 0, null);

            /* Si le client n'a pas "pay�", on ajoute le watermark. Sinon,
               on change l'attribut de paye pour faire en sorte que l'utilisateur
               doive repayer � chaque nouvelle image */
            if (req.getAttribute("paye") != null && req.getAttribute("paye").equals("true")){
                req.setAttribute("paye","false");
            }else{
                g2d = addWatermark(g2d, photo);
            }
             
            //Free graphic resources
            g2d.dispose();

            //Set the mime type of the image
            res.setContentType("image/jpg");

            //Write the image as a jpg
            /***Au lieu de faire l'insertion de l'hyperlien de l'image,
            //   on �crit directement les bits de l'image dans la r�ponse, 
            //   masquant ainsi l'URL de l'image originale */
            OutputStream out = res.getOutputStream();
            ImageIO.write(bufferedImage, "jpg", out);
            out.close();
        }
        catch (IOException ioe)
        {
        }
    }
    
    private Graphics2D addWatermark(Graphics2D g2d, ImageIcon photo){
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                                                              0.5f);
            g2d.setComposite(alpha);

            g2d.setColor(Color.white);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setFont(new Font("Arial", Font.BOLD, 30));

            String watermark = getServletConfig().getInitParameter("message");

            FontMetrics fontMetrics = g2d.getFontMetrics();
            Rectangle2D rect = fontMetrics.getStringBounds(watermark, g2d);

            g2d.drawString(watermark,
                            (photo.getIconWidth() - (int) rect.getWidth()) / 2,
                            (photo.getIconHeight() - (int) rect.getHeight()) / 2);

            return g2d;
        }
}