# hexo-to-mediawiki
A tool for migrating from hexo to mediawiki

```bash

php importImages.php blog/source/images

# use this project to generate the blogs.xml
php importDump.php  blogs.xml
```

you need to create a custom namespace first
```php
define("NS_BLOG", 3000);
define("NS_BLOG_TALK", 3001);

$wgExtraNamespaces[NS_BLOG] = "Blog";
$wgExtraNamespaces[NS_BLOG_TALK] = "Blog_talk";
```
