package com.logan.formstrength;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.*;
/** Only grants a single random app-private capture file, never arbitrary paths. */
public final class MealPhotoProvider extends ContentProvider {
 static final String AUTHORITY="com.logan.formstrength.mealphotos";
 static boolean validName(String name){return name!=null&&name.matches("meal-[a-f0-9-]{36}\\.jpg");}
 static File directory(Context c){return new File(c.getCacheDir(),"meal-captures");}
 static File file(Context c,String name){if(!validName(name))throw new IllegalArgumentException("Invalid meal photo.");return new File(directory(c),name);}
 static Uri uri(String name){if(!validName(name))throw new IllegalArgumentException("Invalid meal photo.");return new Uri.Builder().scheme("content").authority(AUTHORITY).appendPath(name).build();}
 private File target(Uri uri){if(!"content".equals(uri.getScheme())||!AUTHORITY.equals(uri.getAuthority())||uri.getPathSegments().size()!=1||uri.getQuery()!=null||uri.getFragment()!=null)throw new IllegalArgumentException("Invalid meal photo.");return file(getContext(),uri.getLastPathSegment());}
 public boolean onCreate(){return true;}
 public String getType(Uri uri){target(uri);return "image/jpeg";}
 public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){File f=target(uri);MatrixCursor result=new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE});result.addRow(new Object[]{f.getName(),f.length()});return result;}
 public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{File f=target(uri);if(!f.isFile())throw new FileNotFoundException("Capture expired.");int flags;if("w".equals(mode)||"wt".equals(mode)||"rwt".equals(mode))flags=ParcelFileDescriptor.MODE_READ_WRITE|ParcelFileDescriptor.MODE_TRUNCATE;else if("rw".equals(mode))flags=ParcelFileDescriptor.MODE_READ_WRITE;else if("r".equals(mode))flags=ParcelFileDescriptor.MODE_READ_ONLY;else throw new FileNotFoundException("Unsupported mode.");return ParcelFileDescriptor.open(f,flags);}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
 static void prepare(Context c,String name)throws IOException{File d=directory(c);if(!d.isDirectory()&&!d.mkdirs())throw new IOException("Cannot prepare camera.");if(!file(c,name).createNewFile())throw new IOException("Capture already exists.");}
 static void remove(Context c,String name){if(validName(name))file(c,name).delete();}
 static void prune(Context c,String keep){File[] files=directory(c).listFiles();if(files!=null)for(File f:files)if(validName(f.getName())&&!f.getName().equals(keep)&&System.currentTimeMillis()-f.lastModified()>24L*60*60*1000)f.delete();}
 static byte[] image(Context c,String name)throws IOException{
  File f=file(c,name);if(f.length()==0||f.length()>20*1024*1024)throw new IOException("Photo is empty or too large. Retake at a lower camera resolution.");
  BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeFile(f.getPath(),bounds);
  if(bounds.outWidth<1||bounds.outHeight<1||(long)bounds.outWidth*bounds.outHeight>100000000L)throw new IOException("Photo cannot be decoded. Retake it.");
  BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;while(Math.max(bounds.outWidth/options.inSampleSize,bounds.outHeight/options.inSampleSize)>1600)options.inSampleSize*=2;
  Bitmap original=BitmapFactory.decodeFile(f.getPath(),options);if(original==null)throw new IOException("Photo cannot be decoded.");Bitmap adjusted=original;
  try{
   Matrix matrix=new Matrix();int orientation=new ExifInterface(f.getPath()).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
   if(orientation==ExifInterface.ORIENTATION_ROTATE_90)matrix.postRotate(90);else if(orientation==ExifInterface.ORIENTATION_ROTATE_180)matrix.postRotate(180);else if(orientation==ExifInterface.ORIENTATION_ROTATE_270)matrix.postRotate(270);else if(orientation==ExifInterface.ORIENTATION_FLIP_HORIZONTAL)matrix.setScale(-1,1);else if(orientation==ExifInterface.ORIENTATION_FLIP_VERTICAL)matrix.setScale(1,-1);else if(orientation==ExifInterface.ORIENTATION_TRANSPOSE){matrix.setScale(-1,1);matrix.postRotate(270);}else if(orientation==ExifInterface.ORIENTATION_TRANSVERSE){matrix.setScale(-1,1);matrix.postRotate(90);}
   if(!matrix.isIdentity())adjusted=Bitmap.createBitmap(original,0,0,original.getWidth(),original.getHeight(),matrix,true);
   // Re-encoding pixels strips EXIF/GPS and all original metadata.
   for(int quality=85;quality>=35;quality-=10){ByteArrayOutputStream bytes=new ByteArrayOutputStream();if(!adjusted.compress(Bitmap.CompressFormat.JPEG,quality,bytes))throw new IOException("Cannot prepare photo.");if(bytes.size()<=512*1024)return bytes.toByteArray();}
   throw new IOException("Photo is too detailed. Retake from further away.");
  }finally{if(adjusted!=original)adjusted.recycle();original.recycle();}
 }
}
