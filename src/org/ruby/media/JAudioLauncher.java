package org.ruby.media;

import org.ruby.codecs.Codecs;
import org.ruby.net.RubySocket;
import org.ruby.client.ui.Receiver;
import org.ruby.client.ui.Ruby;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;

import android.preference.PreferenceManager;

/** Audio launcher based on javax.sound  */
public class JAudioLauncher implements MediaLauncher
{  
   /** Event logger. */
   Log log=null;

   /** Sample rate [bytes] */
   int sample_rate=8000;
   /** Sample size [bytes] */
   int sample_size=1;
   /** Frame size [bytes] */
   int frame_size=160;
   /** Frame rate [frames per second] */
   int frame_rate=50; //=sample_rate/(frame_size/sample_size);
   boolean signed=false; 
   boolean big_endian=false;

   //String filename="audio.wav"; 

   /** Test tone */
   public static final String TONE="TONE";

   /** Test tone frequency [Hz] */
   public static int tone_freq=100;
   /** Test tone ampliture (from 0.0 to 1.0) */
   public static double tone_amp=1.0;

   /** Runtime media process */
   Process media_process=null;
   
   int dir; // duplex= 0, recv-only= -1, send-only= +1; 

   RubySocket socket=null;
   RtpStreamSender sender=null;
   RtpStreamReceiver receiver=null;
   
   //change DTMF
   boolean useDTMF = false;  // zero means not use outband DTMF
   
   /** Costructs the audio launcher */
   public JAudioLauncher(RtpStreamSender rtp_sender, RtpStreamReceiver rtp_receiver, Log logger)
   {  log=logger;
      sender=rtp_sender;
      receiver=rtp_receiver;
   }

   /** Costructs the audio launcher */
   public JAudioLauncher(int local_port, String remote_addr, int remote_port, int direction, String audiofile_in, String audiofile_out, int sample_rate, int sample_size, int frame_size, Log logger, Codecs.Map payload_type, int dtmf_pt)
   {  log=logger;
      frame_rate=sample_rate/frame_size;
      useDTMF = (dtmf_pt != 0);
      try
      {
    	 CallRecorder call_recorder = null;
    	 if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(org.ruby.client.ui.Settings.PREF_CALLRECORD,
					org.ruby.client.ui.Settings.DEFAULT_CALLRECORD))
    		 call_recorder = new CallRecorder(null,payload_type.codec.samp_rate()); // Autogenerate filename from date. 
    	 socket=new RubySocket(local_port);
         dir=direction;
         // sender
         if (dir>=0)
         {  printLog("new audio sender to "+remote_addr+":"+remote_port,LogLevel.MEDIUM);
            //audio_input=new AudioInput();
            sender=new RtpStreamSender(true,payload_type,frame_rate,frame_size,socket,remote_addr,remote_port,call_recorder);
            sender.setSyncAdj(2);
            sender.setDTMFpayloadType(dtmf_pt);
         }
         
         // receiver
         if (dir<=0)
         {  printLog("new audio receiver on "+local_port,LogLevel.MEDIUM);
            receiver=new RtpStreamReceiver(socket,payload_type,call_recorder);
         }
      }
      catch (Exception e) {  printException(e,LogLevel.HIGH);  }
   }

   /** Starts media application */
   public boolean startMedia()
   {  printLog("starting java audio..",LogLevel.HIGH);

      if (sender!=null)
      {  printLog("start sending",LogLevel.LOW);
         sender.start();
      }
      if (receiver!=null)
      {  printLog("start receiving",LogLevel.LOW);
         receiver.start();
      }
      
      return true;      
   }

   /** Stops media application */
   public boolean stopMedia()
   {  printLog("halting java audio..",LogLevel.HIGH);    
      if (sender!=null)
      {  sender.halt(); sender=null;
         printLog("sender halted",LogLevel.LOW);
      }      
      if (receiver!=null)
      {  receiver.halt(); receiver=null;
         printLog("receiver halted",LogLevel.LOW);
      }      
      if (socket != null)
    	  socket.close();
      return true;
   }

   public boolean muteMedia()
   {
	   if (sender != null)
		   return sender.mute();
	   return false;
   }
   
   public int speakerMedia(int mode)
   {
	   if (receiver != null)
		   return receiver.speaker(mode);
	   return 0;
   }

   public void bluetoothMedia()
   {
	   if (receiver != null)
		   receiver.bluetooth();
   }

   //change DTMF
	/** Send outband DTMF packets **/
  public boolean sendDTMF(char c){
	    if (! useDTMF) return false;
	    sender.sendDTMF(c);
	    return true;
  }
  
   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,LogLevel.HIGH);
   }

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {
	  if (Ruby.release) return;
	  if (log!=null) log.println("AudioLauncher: "+str,level+SipStack.LOG_LEVEL_UA);  
      if (level<=LogLevel.HIGH) System.out.println("AudioLauncher: "+str);
   }

   /** Adds the Exception message to the default Log */
   void printException(Exception e,int level)
   { 
	  if (Ruby.release) return;
	  if (log!=null) log.printException(e,level+SipStack.LOG_LEVEL_UA);
      if (level<=LogLevel.HIGH) e.printStackTrace();
   }

}