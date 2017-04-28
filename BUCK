COMPILE_DEPS = [
    '//lib:CORE_DEPS',
    '//lib:org.apache.karaf.shell.console',
    '//cli:onos-cli',
    '//lib:javax.ws.rs-api',
    '//lib:jersey-server',
    '//utils/rest:onlab-rest',
]

osgi_jar(
    deps = COMPILE_DEPS,
    web_context = '/onos/nai',
)

onos_app(
    app_name = "org.onosproject.nai.intent",
    title = 'Intent utitlty for NAI',
    category = 'Utility',
    url = 'http://onosproject.org',
    description = 'Intent utitlity for nai.',
)