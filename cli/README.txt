CAS-CLI In a Nutshell

There are 2 type of Command Line Option:
- SimpleCmdLineOption
- AdvancedCmdLineOption

An AdvancedCmdLineOption is a SimpleCmdLineOption which is also a ValidatableCmdLineOption and HandleableCmdLineOption, which means that you would like to plug in validator and handlers to your option and let CAS-CLI run them against the argument values given to your options.  If you would like to just have CAS-CLI parse out your arguments and you want to handle and valid them yourself in your code then just use SimpleCmdLineOption.  However, I of course recommend using AdvancedCmdLineOption for reasons which this README was written :).

How do i use CAS-CLI?
First off, start by creating a directory to work in: cli-test. In it create your main class MyMain.java:

import org.apache.oodt.cas.cli.CmdLineUtility;

public class MyMain {

   public static void main(String[] args) {
      CmdLineUtility cmdLineUtility = new CmdLineUtility();
      cmdLineUtility.run(args);
   }
}

Nexted create 2 XML files (put them in the same directory as your MyMain.java): cmd-line-options.xml and cmd-line-actions.xml.  cmd-line-options.xml will contain the declaration of your supported AdvancedCmdLineOption and cmd-line-actions.xml will contain your supported CmdLineActions.  For now though, in order to show you what you get for free, create an cmd-line-options.xml file which defines now AdvancedCmdLineOption:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
</beans>

Next create an cmd-line-actions.xml file with the following CmdLineAction that prints out "Hello World" when it is executed:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

  <bean id="PrintHelloWorldAction" class="org.apache.oodt.cas.cli.action.PrintMessageAction">
    <property name="description" value="Prints out 'Hello World'" />
    <property name="message" value="Hello World" />
  </bean>
</beans>

Now create a logging.properties file in your cli-test directory with the following contents:

org.springframework.beans.level = WARNING
org.springframework.core.level = WARNING
org.springframework.level = WARNING
org.springframework.beans.factory.level = WARNING
org.springframework.beans.factory.config.level = WARNING
org.springframework.beans.factory.config.PropertyPlaceholderConfigurer.level = WARNING


Next create a script: cli-test.sh.  This should go in the cli-test directory and should look like:

#!/bin/sh

java \
  -Djava.ext.dirs=. \
  -Djava.util.logging.config.file=./logging.properties \
        -Dorg.apache.oodt.cas.cli.action.spring.config=./cmd-line-actions.xml \
        -Dorg.apache.oodt.cas.cli.option.spring.config=./cmd-line-options.xml \
  MyMain $*

Then checkout cas-cli and in the same directory as its pom.xml file run (you will need to install maven 2 if you do not have it):

$ mvn package

untar the *-dist.tar file in the target directory and copy all the *.jar files from the lib directory to your cli-test directory

Now compile your MyMain.java:
javac -Djava.ext.dirs=. MyMain.java

Then (in your cli-test directory) execute cli-test.sh:

$ ./cli-test.sh

You should see:
-----------------------------------------------------------------------------------------------------------------
| Short | Long                                             | Description
-----------------------------------------------------------------------------------------------------------------

 -a,     --action <action-name>                             This is the name of the action to trigger
 -h,     --help                                             Prints help menu
 -psa,   --printSupportedActions                            Print Supported Actions
-----------------------------------------------------------------------------------------------------------------

Running cli-test.sh without any arguments is the same as running:

$ ./cli-test.sh -h

or

$ ./cli-test.sh --help

Now to see the list of supported CmdLineAction which you can run, execute:

$ ./cli-test.sh -psa

You should see:
-----------------------------------------------------------------------------------------------------------------
| Action                            | Description
-----------------------------------------------------------------------------------------------------------------
  PrintHelloWorldAction               Prints out 'Hello World'

-----------------------------------------------------------------------------------------------------------------

To see help for a specific action use the help option with the action id given as its argument value:

$ ./cli-test.sh -h PrintHelloWorldAction

You should see:
** Action Help for 'PrintHelloWorldAction' **
> DESCRIPTION:
 Prints out 'Hello World'

> USAGE:
 Required:
   -a [--action] PrintHelloWorldAction
 Optional:

> EXAMPLES:
 - N/A

Now to run PrintHelloWorldAction:

$ ./cli-test.sh -a PrintHelloWorldAction

You should see:
Hello World

So far so easy right?  Now lets take a look at the class for the action (i.e. org.apache.oodt.cas.cli.action.PrintMessageAction) we just ran and see how easy the code for it is:

public class PrintMessageAction extends CmdLineAction {

   private String message;

   @Override
   public void execute(ActionMessagePrinter printer) {
      Validate.notNull(message);

      printer.print(message);
   }

   @Required
   public void setMessage(String message) {
      this.message = message;
   }

   public String getMessage() {
      return message;
   }
}

As your can see it has a setter method for its variable 'message'.  The reason the PrintHelloWorldAction prints out hello is because we set the message property in the cmd-line-actions.xml file for bean id PrintMessageAction to "Hello World":

    <property name="message" value="Hello World" />

If we where to change that to something else, say "Bye World", it would instead print "Bye World" when PrintHelloWorldAction was run (of course then you would probably want to change its id to "ByeHelloWorldAction".  No lets see how easy it is to add our own options.  Let's make another action in cmd-line-actions.xml called "PrintMessageAction" so that cmd-line-actions.xml now looks like this:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

  <bean id="PrintHelloWorldAction" class="org.apache.oodt.cas.cli.action.PrintMessageAction">
    <property name="description" value="Prints out 'Hello World'" />
    <property name="message" value="Hello World" />
  </bean>
  <bean id="PrintMessageAction" class="org.apache.oodt.cas.cli.action.PrintMessageAction">
    <property name="description" value="Prints out 'Hello World'" />
  </bean>
</beans>

Notice we used the same java class and we didn't set the message property.  This is because we are going to now set this property via the command line.  Now in cmd-line-options.xml let's create add an option:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

  <bean id="printMessage" class="org.apache.oodt.cas.cli.option.AdvancedCmdLineOption">
    <property name="shortOption" value="pm" />
    <property name="longOption" value="printMessage" />
    <property name="description" value="Message to print out" />
    <property name="hasArgs" value="true" />
    <property name="argsDescription" value="message" />
    <property name="requirementRules">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.require.ActionDependencyRule"
          p:actionName="PrintMessageAction" p:relation="REQUIRED" />
      </list>
    </property>
    <property name="handler">
      <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler">
        <property name="applyToActions">
          <list>
            <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToAction"
              p:actionName="PrintMessageAction" p:methodName="setMessage" />
          </list>
        </property>
      </bean>
    </property>
  </bean>
</beans>

Now run:

$ ./cli-test.sh -h

You should see:
-----------------------------------------------------------------------------------------------------------------
| Short | Long                                             | Description
-----------------------------------------------------------------------------------------------------------------

 -a,     --action <action-name>                             This is the name of the action to trigger
 -pm,    --printMessage <message>                           Message to print out
                                                              Requirement Rules: 
                                                               [PrintMessageAction : REQUIRED] 

                                                              Handler: 
                                                               Will invoke 'setPrintMessage' on action selected, 
                                                               except for the following actions: 
                                                               [PrintMessageAction : setMessage] 

 -h,     --help                                             Prints help menu
 -psa,   --printSupportedActions                            Print Supported Actions
-----------------------------------------------------------------------------------------------------------------

Notice now that we have an option --printMessage.  The description we put the for option printMessage in cmd-line-options.xml is now under description in your command line help.  Also notice the requirement rules.  The requirement rules setup for this option is: if PrintMessageAction is executed then printMessage option becomes required.  You can also see this requirement rule when running:

$ ./cli-test.sh -h PrintMessageAction

You should see:
** Action Help for 'PrintMessageAction' **
> DESCRIPTION:
 Prints out 'Hello World'

> USAGE:
 Required:
   -a [--action] PrintMessageAction
   -pm [--printMessage] <message>
 Optional:

> EXAMPLES:
 - N/A

Notice that printMessage is now under the required options.  Now here is the really cool magic.  Run:

$ ./cli-test.sh -a PrintMessageAction -pm "Hocus Pocus"

You should see:
Hocus Pocus

We can now dynamically tell it what we want it to print out.  How does this work?  This works because we assigned a handler to the printMessage option.  The handler assigned was: org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler.  This handler takes a list of org.apache.oodt.cas.cli.option.handler.ApplyToAction beans which tell it the actionName and (optionally) the method name to invoke on that action.  So when this option is specified, the value given to the option (in this case was "Hocus Pocus") was then passed as the argument to the method setMessage(String) on the action PrintMessageAction.

Options can also have static argument values, which can be used as a default argument value or as a permanent argument for an option.  Let's modify the current example to show what i mean.  Here is how you would use static argument values to make "Hello World" the default:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

  <bean id="printMessage" class="org.apache.oodt.cas.cli.option.AdvancedCmdLineOption">
    <property name="shortOption" value="pm" />
    <property name="longOption" value="printMessage" />
    <property name="description" value="Message to print out" />
    <property name="hasArgs" value="true" />
    <property name="argsDescription" value="message" />
    <property name="staticArgs">
            <list>
              <value>Hello World</value>
            </list>
        </property>
    <property name="requirementRules">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.require.ActionDependencyRule"
          p:actionName="PrintMessageAction" p:relation="REQUIRED" />
      </list>
    </property>
    <property name="handler">
      <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler">
        <property name="applyToActions">
          <list>
            <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToAction"
              p:actionName="PrintMessageAction" p:methodName="setMessage" />
          </list>
        </property>
      </bean>
    </property>
  </bean>
</beans>

Now execute the following:

$ ./cli-test.sh -a PrintMessageAction -pm

You should see:
Hello World

Then execute:

./cli-test.sh -a PrintMessageAction -pm "Hocus Pocus"

You should see:
Hocus Pocus

So the static argument value made printMessage option value optional.  Now the other way to use static arguments is to make an option not support argument values and use the static argument value as its permanent value.  Modify your cmd-line-options.xml to look like the following:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

  <bean id="printMessage" class="org.apache.oodt.cas.cli.option.AdvancedCmdLineOption">
    <property name="shortOption" value="pm" />
    <property name="longOption" value="printMessage" />
    <property name="description" value="Message to print out" />
    <property name="hasArgs" value="false" />
    <property name="staticArgs">
            <list>
              <value>Hello World</value>
            </list>
        </property>
    <property name="requirementRules">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.require.ActionDependencyRule"
          p:actionName="PrintMessageAction" p:relation="REQUIRED" />
      </list>
    </property>
    <property name="handler">
      <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler">
        <property name="applyToActions">
          <list>
            <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToAction"
              p:actionName="PrintMessageAction" p:methodName="setMessage" />
          </list>
        </property>
      </bean>
    </property>
  </bean>
</beans>

Now execute the following:

$ ./cli-test.sh -h PrintMessageAction

You should see:

** Action Help for 'PrintMessageAction' **
> DESCRIPTION:
 Prints out 'Hello World'

> USAGE:
 Required:
   -a [--action] PrintMessageAction
   -pm [--printMessage]
 Optional:

> EXAMPLES:
 - N/A

Notice that -pm does have <message> anymore, this means it doesn't support arguments.  Now execute:

$ ./cli-test.sh -a PrintMessageAction -pm

You should see:
Hello World

Now how is this useful, less change the the cmd-line-options.xml file, renaming printMessage option to printHelloWorld and add another action printByeWorld:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

  <bean id="printHelloWorld" class="org.apache.oodt.cas.cli.option.AdvancedCmdLineOption">
    <property name="shortOption" value="phw" />
    <property name="longOption" value="printHelloWorld" />
    <property name="description" value="Print out 'Hello World'" />
    <property name="hasArgs" value="false" />
    <property name="staticArgs">
            <list>
              <value>Hello World</value>
            </list>
        </property>
    <property name="requirementRules">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.require.ActionDependencyRule"
          p:actionName="PrintMessageAction" p:relation="OPTIONAL" />
      </list>
    </property>
    <property name="handler">
      <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler">
        <property name="applyToActions">
          <list>
            <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToAction"
              p:actionName="PrintMessageAction" p:methodName="setMessage" />
          </list>
        </property>
      </bean>
    </property>
  </bean>

  <bean id="printByeWorld" class="org.apache.oodt.cas.cli.option.AdvancedCmdLineOption">
    <property name="shortOption" value="pbw" />
    <property name="longOption" value="printByeWorld" />
    <property name="description" value="Print out 'Bye World'" />
    <property name="hasArgs" value="false" />
    <property name="staticArgs">
            <list>
              <value>Bye World</value>
            </list>
        </property>
    <property name="requirementRules">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.require.ActionDependencyRule"
          p:actionName="PrintMessageAction" p:relation="OPTIONAL" />
      </list>
    </property>
    <property name="handler">
      <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler">
        <property name="applyToActions">
          <list>
            <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToAction"
              p:actionName="PrintMessageAction" p:methodName="setMessage" />
          </list>
        </property>
      </bean>
    </property>
  </bean>
</beans>

There are now 2 options, but i've set them both to optional because only one should be specified.  However, this is a problem because we want at least 1 specified (this will be addressed next).  For now let's see how these two options with static argument values work.  Execute the following:

$ ./cli-test.sh -h PrintMessageAction

You should see:
** Action Help for 'PrintMessageAction' **
> DESCRIPTION:
 Prints out 'Hello World'

> USAGE:
 Required:
   -a [--action] PrintMessageAction
 Optional:
   -pbw [--printByeWorld]
   -phw [--printHelloWorld]

> EXAMPLES:
 - N/A

Our two options now appear under the optional section.  Now execute:

$ ./cli-test.sh -a PrintMessageAction -pbw

You should see:
Bye World

Then execute:

$ ./cli-test.sh -a PrintMessageAction -phw

You should see:
Hello World

Now this works nice, except cas-cli is able to enforce one of the options (i.e. -pbw and -phw) to be specific so if you run the following it causes the action to print out an ERROR:

$ ./cli-test.sh -a PrintMessageAction

You should see:
ERROR: Must specify message

We can prevent the PrintMessageAction from having to throw this exception by, creating a group option and adding both -pbw and -phw to it.  Modify your cmd-line-options.xml to the following:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

  <bean id="print" class="org.apache.oodt.cas.cli.option.GroupCmdLineOption">
    <property name="shortOption" value="p" />
    <property name="longOption" value="print" />
    <property name="description" value="Declare that you wish to print a message" />
    <property name="hasArgs" value="false" />
    <property name="requirementRules">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.require.ActionDependencyRule"
          p:actionName="PrintMessageAction" p:relation="REQUIRED" />
      </list>
    </property>
    <property name="subOptions">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.GroupSubOption"
          p:option-ref="helloWorld" p:required="false" />
        <bean class="org.apache.oodt.cas.cli.option.GroupSubOption"
          p:option-ref="byeWorld" p:required="false" />
      </list>
    </property>
    </bean>

  <bean id="helloWorld" class="org.apache.oodt.cas.cli.option.AdvancedCmdLineOption" p:isSubOption="true">
    <property name="shortOption" value="hw" />
    <property name="longOption" value="helloWorld" />
    <property name="description" value="Print out 'Hello World'" />
    <property name="hasArgs" value="false" />
    <property name="staticArgs">
            <list>
              <value>Hello World</value>
            </list>
        </property>
    <property name="requirementRules">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.require.ActionDependencyRule"
          p:actionName="PrintMessageAction" p:relation="OPTIONAL" />
      </list>
    </property>
    <property name="handler">
      <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler">
        <property name="applyToActions">
          <list>
            <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToAction"
              p:actionName="PrintMessageAction" p:methodName="setMessage" />
          </list>
        </property>
      </bean>
    </property>
  </bean>

  <bean id="byeWorld" class="org.apache.oodt.cas.cli.option.AdvancedCmdLineOption" p:isSubOption="true">
    <property name="shortOption" value="bw" />
    <property name="longOption" value="byeWorld" />
    <property name="description" value="Print out 'Bye World'" />
    <property name="hasArgs" value="false" />
    <property name="staticArgs">
            <list>
              <value>Bye World</value>
            </list>
        </property>
    <property name="requirementRules">
      <list>
        <bean class="org.apache.oodt.cas.cli.option.require.ActionDependencyRule"
          p:actionName="PrintMessageAction" p:relation="OPTIONAL" />
      </list>
    </property>
    <property name="handler">
      <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler">
        <property name="applyToActions">
          <list>
            <bean class="org.apache.oodt.cas.cli.option.handler.ApplyToAction"
              p:actionName="PrintMessageAction" p:methodName="setMessage" />
          </list>
        </property>
      </bean>
    </property>
  </bean>
</beans>

So what was changed here is 'printHelloWorld' was renamed to 'helloWorld' and p:isSubOption="true" was added to it (similar was done with 'printByeWorld').  Also a group option was created and 'helloWorld' and 'byeWorld' where added as optional sub-options.  When all sub-options of a group option are option, then one of the sub-options will be required.  Now execute:

$ ./cli-test.sh -h

You should see:
-----------------------------------------------------------------------------------------------------------------
| Short | Long                                             | Description
-----------------------------------------------------------------------------------------------------------------

 -a,     --action <action-name>                             This is the name of the action to trigger
 -p,     --print                                            Declare that you wish to print a message
                                                              Requirement Rules: 
                                                               [PrintMessageAction : REQUIRED] 

   SubOptions:
   > Required:
   > Optional:
      -hw,    --helloWorld                                  Print out 'Hello World'
                                                              Requirement Rules: 
                                                               [PrintMessageAction : OPTIONAL] 

                                                              Handler: 
                                                               Will invoke 'setHelloWorld' on action selected, 
                                                               except for the following actions: 
                                                               [PrintMessageAction : setMessage] 
      -bw,    --byeWorld                                    Print out 'Bye World'
                                                              Requirement Rules: 
                                                               [PrintMessageAction : OPTIONAL] 

                                                              Handler: 
                                                               Will invoke 'setByeWorld' on action selected, 
                                                               except for the following actions: 
                                                               [PrintMessageAction : setMessage] 

 -h,     --help                                             Prints help menu
 -psa,   --printSupportedActions                            Print Supported Actions
-----------------------------------------------------------------------------------------------------------------

Then execute:

$ ./cli-test.sh -h PrintMessageAction

You should see:
** Action Help for 'PrintMessageAction' **
> DESCRIPTION:
 Prints out 'Hello World'

> USAGE:
 Required:
   -a [--action] PrintMessageAction
   -p [--print]
     One of:
      -hw [--helloWorld]
      -bw [--byeWorld]
 Optional:

> EXAMPLES:
 - N/A

Notice how "One of:" is listed under -p option, that means the on of the optional sub-options must be specified.  Now let's rerun the command which caused PrintMessageAction to throw the ERROR:

$ ./cli-test.sh -a PrintMessageAction

You should see:
ERROR: Missing required options:
 - [longOption='print',shortOption='p',description='Declare that you wish to print a message']

This time cas-cli was able to realize that you are missing an option and tell you to set it.  Now let's add the -p option to the previous command:

$ ./cli-test.sh -a PrintMessageAction -p

You should see:
ERROR: Must specify a subOption for group option '[longOption='print',shortOption='p',description='Declare that you wish to print a message']'

You are now told by cas-cli that you need to specify a sup-option for print (thus solving the problem we had before with two optional options).  Now execute the following:

$ ./cli-test.sh -a PrintMessageAction -p -hw

You should see:
Hello World

Then execute:

/cli-test.sh -a PrintMessageAction -p -bw

You should see:
Bye World

As you can see, still the same output as before without the grouping, but have we now added the ability to have list of optional options where at least one is required.