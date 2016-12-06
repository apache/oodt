module Jekyll
  class PMCTag < Liquid::Tag
  require 'net/http'
  require 'uri'
  require 'json'

     def open(url)
        Net::HTTP.get(URI.parse(url))
    end
    def initialize(tag_name, text, tokens)
      super
      @project = text
    end

    def getPMC(project)
      @userlist = Array.new
      ldapcontent = open("https://whimsy.apache.org/public/public_ldap_committees.json")
      ldapjson = JSON.parse(ldapcontent)
      ldappeople = open("https://whimsy.apache.org/public/public_ldap_people.json")
      peoplejson = JSON.parse(ldappeople)

      use = ldapjson["committees"]["oodt"]["roster"].each do |person|
        @userlist << peoplejson["people"][person]["name"]+"<br/>"
      end 
    end
      def render(context)
        getPMC(@project)

        @userlist
    end
  end
end

Liquid::Template.register_tag('pcm_list', Jekyll::PMCTag)
