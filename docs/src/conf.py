# -*- coding: utf-8 -*-

import os
import sys

from datetime import datetime

import javalink

# -- General configuration ------------------------------------------------
# See http://sphinx-doc.org/config.html for details

sys.path.append(os.path.dirname(os.path.realpath(__file__)))
extensions = ['javalink', 'gh-pages']

project = u'Giraffe'
copyright = u'{}, Palantir Technologies'.format(datetime.now().year)

release = os.environ.get('GIRAFFE_VERSION', 'unknown')
if '-' in release:
    version = release.split('-')[0]
else:
    version = release

master_doc = 'index'
source_suffix = '.rst'

exclude_patterns = []
templates_path = ['templates']

# The name of the Pygments (syntax highlighting) style to use.
pygments_style = 'sphinx'
highlight_language = 'java'

# Assume javadoc was already build by Gradle
javalink_classpath = [
    javalink.find_rt_jar(),
    '../../core/build/classes/java/main',
    '../../ssh/build/classes/java/main',
    '../../fs-base/build/classes/java/main'
]

javalink_docroots = [
    'http://docs.oracle.com/javase/8/docs/api/',
    {'root': '../build/javadoc', 'base': 'api'}
]

javalink_default_version = 8
javalink_add_package_names = False

# -- Options for HTML output ----------------------------------------------
# See http://sphinx-doc.org/config.html#html-options for details

html_static_path = ['static']
html_show_sourcelink = False
html_theme = 'alabaster'
html_theme_options = {
    'logo': 'logo.png',
    'logo_name': True,
    'logo_text_align': 'center',
    'github_button': False,
    'extra_nav_links': {
        'Main Page': 'http://palantir.github.io/giraffe/',
        'GitHub': 'https://github.com/palantir/giraffe'
    },
    'page_width': '90%',
    'sidebar_width': '230px'
}
html_sidebars = {
    '**': [
        'about.html',
        'navigation.html',
        'searchbox.html'
    ]
}
