using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Reflection;

namespace Test
{
    class Program
    {
        static void Main(string[] args2)
        {
            //
            // Submit photo
            //

            {
                String path = Assembly.GetExecutingAssembly().Location;
                path = Path.GetDirectoryName(path);
                path = path + "\\test.jpg";
                byte[] picBytes = File.ReadAllBytes(path);
                String picBase64 = Convert.ToBase64String(picBytes);

                Dictionary<String, String> args = new Dictionary<string, string>();
                args.Add("key", "13564311086");

                String result = name.zwc.Caller.Handlers.ContactPhotoHelper.SubmitPhoto(picBase64, args);
                Console.WriteLine(result);
            }

            //
            // Check Update
            // 

            {
                String input = Convert.ToBase64String(picBytes);

                String result = name.zwc.Caller.Handlers.ContactPhotoHelper.SubmitPhoto(input, null);
                Console.WriteLine(result);
            }

            Console.WriteLine("Done");
            Console.ReadKey();
        }
    }
}
